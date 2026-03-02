package com.thc.sprbasic2025.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.thc.sprbasic2025.domain.Permission;
import com.thc.sprbasic2025.domain.Permissiondetail;
import com.thc.sprbasic2025.domain.Permissionuser;
import com.thc.sprbasic2025.domain.User;
import com.thc.sprbasic2025.dto.PermissionDto;
import com.thc.sprbasic2025.dto.UserDto;
import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.exception.NoMatchingDataException;
import com.thc.sprbasic2025.mapper.PermissionMapper;
import com.thc.sprbasic2025.mapper.UserMapper;
import com.thc.sprbasic2025.repository.PermissionRepository;
import com.thc.sprbasic2025.repository.PermissiondetailRepository;
import com.thc.sprbasic2025.repository.PermissionuserRepository;
import com.thc.sprbasic2025.repository.UserRepository;
import com.thc.sprbasic2025.security.AuthService;
import com.thc.sprbasic2025.security.ExternalProperties;
import com.thc.sprbasic2025.service.PermittedService;
import com.thc.sprbasic2025.service.UserService;
import com.thc.sprbasic2025.util.MailBox;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.util.*;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class UserServiceimpl implements UserService {

    final String target = "user";
    private static final int GOOGLE_VERIFY_CONNECT_TIMEOUT_MS = 5000;
    private static final int GOOGLE_VERIFY_READ_TIMEOUT_MS = 5000;

    final UserRepository userRepository;
    final PermissionRepository permissionRepository;
    final PermissiondetailRepository permissiondetailRepository;
    final PermissionuserRepository permissionuserRepository;
    final PermissionMapper permissionMapper;
    final UserMapper userMapper;
    final AuthService authService;
    final BCryptPasswordEncoder bCryptPasswordEncoder;
    final PermittedService permittedService;
    final MailBox mailBox;
    final ExternalProperties externalProperties;
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @PostConstruct
    public void initGoogleVerifier() {
        HttpTransport transport = new NetHttpTransport.Builder()
                .setConnectionFactory(url -> {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(GOOGLE_VERIFY_CONNECT_TIMEOUT_MS);
                    connection.setReadTimeout(GOOGLE_VERIFY_READ_TIMEOUT_MS);
                    return connection;
                })
                .build();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(externalProperties.getGoogleClientId()))
                .build();
    }

    @Override
    public String google(String idTokenString) {
        String googleSub = null;
        String name = null;
        String email = null;

        try {
            GoogleIdToken idToken = googleIdTokenVerifier.verify(idTokenString);
            if (idToken == null) {
                throw new RuntimeException("Invalid Google ID token");
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            googleSub = payload.getSubject();
            email = payload.getEmail();
            name = (String) payload.get("name");
        } catch (Exception e) {
            throw new RuntimeException("Google token verification failed", e);
        }

        if (googleSub == null) {
            throw new RuntimeException("Google Info not found");
        }

        if (email == null || !email.contains("@handong.")) {
            return "not_valid_email";
        }

        String username = googleSub;
        Long id;
        User user = userRepository.findByUsername(username);
        if (user == null) {
            String password = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            DefaultDto.CreateResDto res = create(
                    UserDto.CreateReqDto.builder()
                            .username(username)
                            .email(email)
                            .password(password)
                            .nick(name)
                            .rfrom(3100)
                            .build(),
                    (long) -200
            );
            id = res.getId();
        } else {
            if (email != null && (user.getEmail() == null || !email.equals(user.getEmail()))) {
                user.setEmail(email);
                userRepository.save(user);
            }
            id = user.getId();
        }

        ensureStudentPermissionAssigned(id);
        return authService.createRefreshToken(id);
    }

    private void ensureStudentPermissionAssigned(Long userId) {
        if (permissionMapper.permitted(
                PermissionDto.PermittedReqDto.builder().userId(userId).target("admin").func(200).build()
        ) > 0) {
            return;
        }

        Permission studentPermission = permissionRepository.findByTitle("일반학생");
        if (studentPermission == null) {
            studentPermission = permissionRepository.save(
                    Permission.of("일반학생", "일반 학생 기본 권한")
            );
        } else if (Boolean.TRUE.equals(studentPermission.getDeleted())) {
            studentPermission.setDeleted(false);
            studentPermission = permissionRepository.save(studentPermission);
        }

        String[] studentTargets = {"notice", "board", "news_letter", "extracurricular_activities"};
        for (String studentTarget : studentTargets) {
            Permissiondetail detail = permissiondetailRepository.findByPermissionIdAndTargetAndFunc(
                    studentPermission.getId(), studentTarget, 200
            );
            if (detail == null) {
                permissiondetailRepository.save(
                        Permissiondetail.of(studentPermission.getId(), studentTarget, 200)
                );
            } else if (Boolean.TRUE.equals(detail.getDeleted())) {
                detail.setDeleted(false);
                permissiondetailRepository.save(detail);
            }
        }

        Permissionuser permissionuser = permissionuserRepository.findByPermissionIdAndUserId(studentPermission.getId(), userId);
        if (permissionuser == null) {
            permissionuserRepository.save(Permissionuser.of(studentPermission.getId(), userId));
        } else if (Boolean.TRUE.equals(permissionuser.getDeleted())) {
            permissionuser.setDeleted(false);
            permissionuserRepository.save(permissionuser);
        }
    }

    @Override
    public boolean nick(UserDto.NickReqDto param, Long reqUserId) {
        User user = userRepository.findByNick(param.getNick());
        if(user != null){
            if(user.getId().equals(reqUserId)){
                return true;
            }
        }
        return (user == null);
    }
    @Override
    public DefaultDto.CreateResDto signup(UserDto.CreateReqDto param, Long reqUserId) {
        // 권한이 없어도 하게 해줘야 함..
        param.setRfrom(1000);
        return create(param, (long) -200);
    }

    @Override
    public DefaultDto.CreateResDto create(UserDto.CreateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 110);
        User user = userRepository.findByUsername(param.getUsername());
        if(user != null){
            throw new RuntimeException("already exist");
        }
        param.setPassword(bCryptPasswordEncoder.encode(param.getPassword()));
        String code = UUID.randomUUID().toString().replace("-", "").substring(0,8);
        param.setCode(code);

        String nick = param.getNick();
        if(nick == null || nick.isEmpty()){
            param.setNick(code);
        } else {
            User userForNick = userRepository.findByNick(nick);
            if(userForNick != null){
                param.setNick(code);
            }
        }

        User newUser = userRepository.save(param.toEntity());
        return newUser.toCreateResDto();
    }

    @Override
    public void update(UserDto.UpdateReqDto param, Long reqUserId) {
        if(param.getId() == 0){ param.setId(reqUserId); }
        if(!param.getId().equals(reqUserId)){
            permittedService.isPermitted(reqUserId, target, 120);
        }

        User user = userRepository.findById(param.getId()).orElseThrow(() -> new NoMatchingDataException("no data"));
        if(param.getPassword() != null){ param.setPassword(bCryptPasswordEncoder.encode(param.getPassword())); }
        user.update(param);

        //System.out.println(user.getBirthyear());

        userRepository.save(user);
    }

    @Override
    public void delete(DefaultDto.DeleteReqDto param, Long reqUserId) {
        update(UserDto.UpdateReqDto.builder().id(param.getId()).deleted(true).build(), reqUserId);
    }

    public UserDto.DetailResDto get(DefaultDto.DetailReqDto param, Long reqUserId) {
        //본인 정보인 경우 확인
        if(!param.getId().equals(reqUserId)){
            permittedService.isPermitted(reqUserId, target, 200);
        }
        UserDto.DetailResDto res = userMapper.detail(param.getId());
        return res;
    }
    @Override
    public UserDto.DetailResDto detail(DefaultDto.DetailReqDto param, Long reqUserId) {
        //본인 정보인 경우 확인
        if(param.getId() == 0){ param.setId(reqUserId); }
        return get(param, reqUserId);
    }

    @Override
    public List<UserDto.DetailResDto> list(UserDto.ListReqDto param, Long reqUserId) {
        return detailList(userMapper.list(param),reqUserId);
    }
    public List<UserDto.DetailResDto> detailList(List<UserDto.DetailResDto> list, Long reqUserId){
        List<UserDto.DetailResDto> newList = new ArrayList<>();
        for(UserDto.DetailResDto each : list){
            newList.add(get(DefaultDto.DetailReqDto.builder().id(each.getId()).build(), reqUserId));
        }
        return newList;
    }

    @Override
    public DefaultDto.PagedListResDto pagedList(UserDto.PagedListReqDto param, Long reqUserId) {
        DefaultDto.PagedListResDto res = param.init(userMapper.pagedListCount(param));
        res.setList(detailList(userMapper.pagedList(param), reqUserId));
        return res;
    }

    @Override
    public List<UserDto.DetailResDto> scrollList(UserDto.ScrollListReqDto param, Long reqUserId) {
        param.init();
        return detailList(userMapper.scrollList(param), reqUserId);
    }


}
