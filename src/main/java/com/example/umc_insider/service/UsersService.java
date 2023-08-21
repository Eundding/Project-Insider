package com.example.umc_insider.service;

import com.example.umc_insider.config.BaseException;
import com.example.umc_insider.config.BaseResponseStatus;
import com.example.umc_insider.domain.Address;
import com.example.umc_insider.domain.Users;
import com.example.umc_insider.domain.UsersImages;
import com.example.umc_insider.dto.request.*;
import com.example.umc_insider.dto.response.GetUserRes;
import com.example.umc_insider.dto.response.PostLoginRes;
import com.example.umc_insider.dto.response.PostUserRes;
import com.example.umc_insider.repository.AddressRepository;
import com.example.umc_insider.repository.UserImageRepository;
import com.example.umc_insider.utils.JwtService;
import com.example.umc_insider.utils.SHA256;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.umc_insider.repository.UserRepository;
import org.springframework.web.multipart.MultipartFile;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.umc_insider.config.BaseResponseStatus.*;

@RequiredArgsConstructor
@Service
public class UsersService {
    private UserRepository userRepository;
    private AddressRepository addressRepository;
    private UserImageRepository userImageRepository;
    private final JwtService jwtService;
    private final S3Service s3Service;

    @Autowired
    public UsersService(UserRepository userRepository, UserImageRepository userImageRepository, JwtService jwtService, AddressRepository addressRepository, S3Service s3Service) {
        this.userRepository = userRepository;
        this.userImageRepository = userImageRepository;
        this.jwtService = jwtService;
        this.addressRepository = addressRepository;
        this.s3Service = s3Service;
    }

    public PostUserRes createUser(PostUserReq postUserReq) throws BaseException {
        Users newUser = postUserReq.createUserWithAddress();
        Address newAddress = newUser.getAddress();

        Users savedUser = saveUserWithAddress(newUser, newAddress);
        return new PostUserRes(savedUser.getId(), savedUser.getNickname());
    }

    public List<GetUserRes> getAllUsers() {
        List<Users> users = userRepository.findAll();
        return mapToUserResponseList(users);
    }


    private List<GetUserRes> mapToUserResponseList(List<Users> users) {
        List<GetUserRes> userResponses = new ArrayList<>();
        for (Users user : users) {
            userResponses.add(new GetUserRes(user.getId(), user.getUser_id(), user.getNickname(), user.getEmail(), user.getPw(), user.getAddress()));
        }
        return userResponses;
    }

    // 특정 유저조회
    public List<GetUserRes> getReferenceById(long id) throws BaseException {
        List<Users> users = userRepository.findAllById(id);
        return mapToUserResponseList(users);
    }


    /**
     * 유저 로그인
     */
    public PostLoginRes logIn(PostLoginReq postLoginReq) throws BaseException {
        Users users = userRepository.findUserByUserId(postLoginReq.getUserId());
        String encryptPw;
        try {
            encryptPw = new SHA256().encrypt(postLoginReq.getPw());
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }


        String originalEncryptPw = new SHA256().encrypt(users.getPw());
        if (originalEncryptPw.equals(encryptPw)) {
            String jwt = jwtService.createJwt(users.getId());
            return new PostLoginRes(users.getId(), jwt);
        } else {
            throw new BaseException(FAILED_TO_LOGIN);
        }
    }

    // 사용자 주소를 가져오는 메소드
    public Address getAddressForUser(Long userId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return user.getAddress();
    }

    @Transactional
    public Users saveUserWithAddress(Users user, Address address) {
        address.setUser(user);
        user.setAddress(address);
        addressRepository.save(address);
        return userRepository.save(user);
    }


    // 이미지 수정/등록
    @Transactional
    public void putUserImg(PutUserImgReq putUserImgReq,  MultipartFile file) {

        UsersImages usersImage = userImageRepository.getReferenceById(putUserImgReq.getUserId());
        usersImage.putImg(putUserImgReq.getImg_url());
    }

    // 유저 프로필 이미지 등록
    public Users registerProfile(PostUserProfileReq postUserProfileReq, MultipartFile file){
        Users user = userRepository.findUsersById(postUserProfileReq.getId());
        userRepository.save(user);
        // S3에 이미지 업로드 및 URL 받기
        String imageUrl = s3Service.uploadProfileS3(file, user);

        // 이미지 URL 설정 후, 객체 업데이트
        user.setImageUrl(imageUrl);

        userRepository.save(user);
        return user;

    }

    // 유저 정보 수정
    public PostUserRes modifyUser(PutUserReq putUserReq) throws BaseException {
        // 요청 객체에서 유저 ID 값 가져오기
        Long id = putUserReq.getId();
        // 유저 ID를 기반으로 유저 정보 가져오기
        Optional<Users> optionalUser = userRepository.findById(id);
        Users userToModify;

        if (optionalUser.isPresent()) {
            userToModify = optionalUser.get();
        } else {
            throw new BaseException(BaseResponseStatus.REQUEST_ERROR);
        }

        // 요청 본문에서 수정할 필드 값들 가져오기
        String nickname = putUserReq.getNickname();
        String password = putUserReq.getPw();
        String userId = putUserReq.getUserId();
        String email = putUserReq.getEmail();
        Integer zipCode = putUserReq.getZipCode();
        String detailAddress = putUserReq.getDetailAddress();

        // 업데이트할 필드만 적용하기
        if (nickname != null) {
            userToModify.setNickname(nickname);
        }
        if (password != null) {
            userToModify.setPw(password);
        }
        if (email != null) {
            userToModify.setEmail(email);
        }
        if (userId != null) {
            userToModify.setUser_id(userId);
        }
        Address newAddress = new Address();
        if (zipCode != null && detailAddress != null) {
            newAddress.setZipCode(zipCode);
            newAddress.setDetailAddress(detailAddress);
            userToModify.setAddress(newAddress);
        }
        userRepository.save(userToModify);
        return new PostUserRes(userToModify.getId(), userToModify.getNickname());
    }
}




