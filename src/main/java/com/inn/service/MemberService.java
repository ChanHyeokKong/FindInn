package com.inn.service;

import com.inn.data.chat.ChatDto;
import com.inn.data.chat.ChatRepository;
import com.inn.data.chat.ChatRoomDto;
import com.inn.data.chat.ChatRoomRepository;
import com.inn.data.hotel.HotelEntity;
import com.inn.data.hotel.HotelRepository;
import com.inn.data.member.*;
import com.inn.data.member.manager.ManageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.inn.config.CustomUserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Transactional import 추가

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.UUID; // UUID import 추가

@Service
public class MemberService implements UserDetailsService {

    @Autowired
    MemberDaoInter memberDao;

    @Autowired
    RoleDaoInter roleDao;

    @Autowired
    SocialMemberRepository socialMemberRepository; // SocialMemberRepository 주입

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    ChatRepository chatRepository;

    @Autowired
    HotelRepository hotelRepository;

    PasswordEncoder pe = new BCryptPasswordEncoder();
    @Autowired
    private MemberDaoInter memberDaoInter;

    //회원가입
    public void signup(MemberDto memberDto) {
        memberDto.setMemberPassword(pe.encode(memberDto.getMemberPassword()));

        // 기본 역할 (ROLE_USER)을 데이터베이스에서 조회하여 추가
        Optional<RoleDto> userRoleOptional = Optional.ofNullable(roleDao.findByRoleName("ROLE_USER"));
        if (userRoleOptional.isPresent()) {
            memberDto.getRoles().add(userRoleOptional.get());
        } else {
            System.out.println("error");
        }
        memberDao.save(memberDto);

    }

    // 기존 유저 Role 제거 후 MANAGER 권한 부여
    public void giveManagerRole(Long memberIdx) {
        Optional<MemberDto> memberOptional = memberDao.findById(memberIdx);

        if (memberOptional.isPresent()) {
            MemberDto memberDto = memberOptional.get();

            Optional<RoleDto> managerRoleOptional = Optional.ofNullable(roleDao.findByRoleName("ROLE_MANAGER"));
            if (managerRoleOptional.isPresent()) {
                memberDto.getRoles().clear();
                memberDto.getRoles().add(managerRoleOptional.get());
                memberDao.save(memberDto);
                System.out.println("Member " + memberIdx + " has been granted ROLE_MANAGER.");
            } else {
                System.err.println("Error: ROLE_MANAGER not found in database. Please ensure it exists.");
            }
        } else {
            System.err.println("Error: Member with ID " + memberIdx + " not found.");
        }
    }

    //멤버 객체 전체 리턴
    public List<MemberDto> getAllMembers() {
        List<MemberDto> list = memberDao.findAll();
        return list;
    }

    // 역할 객체 전체 리턴
    public List<RoleDto> getAllRoles() {
        List<RoleDto> list = roleDao.findAll();
        return list;
    }

    // 이메일을 통해 멤버 객체 리턴 -> 중복체크 용
    public MemberDto getMemberByEmail(String email) {
        return memberDao.findByMemberEmail(email);
    }


    // 멤버 아이디를 검색 -> 멤버 있는지 체크
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MemberDto member = memberDao.findByMemberEmail(username);
        if (member == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        return new CustomUserDetails(member);
    }

    // MemberIdx(CurrentUser) 통해 예약 정보 리턴
    public List<MyPageDto> getMyReserve(Long memberIdx) {
        return memberDaoInter.findMyReserve(memberIdx);
    }

    // 소셜 로그인 처리 (네이버, 카카오 등)
    @Transactional // 트랜잭션 어노테이션 추가
    public UserDetails processSocialLogin(String socialProvider, String socialProviderKey, String email, String name, String mobile) {
        Optional<SocialMemberDto> socialMemberOptional = socialMemberRepository.findBySocialProviderAndSocialProviderKey(socialProvider, socialProviderKey);

        MemberDto member;
        if (socialMemberOptional.isPresent()) {
            // 이미 가입된 소셜 회원
            SocialMemberDto socialMember = socialMemberOptional.get();
            member = memberDao.findById(socialMember.getMemberIdx())
                    .orElseThrow(() -> new UsernameNotFoundException("Member not found for social login: " + email));
            // 필요하다면 여기서 회원 정보 업데이트 로직 추가
        } else {
            // 새로운 소셜 회원 또는 기존 이메일 회원과 연동
            member = memberDao.findByMemberEmail(email);
            if (member == null) {
                // 완전히 새로운 회원 (소셜 + 이메일 모두 없음)
                member = new MemberDto();
                member.setMemberEmail(email);
                member.setMemberName(name);
                member.setMemberPassword(null);
                member.setMemberPhone(mobile); // 네이버에서 가져온 전화번호 설정

                // 기본 역할 (ROLE_USER) 부여
                Optional<RoleDto> userRoleOptional = Optional.ofNullable(roleDao.findByRoleName("ROLE_USER"));
                if (userRoleOptional.isPresent()) {
                    member.getRoles().add(userRoleOptional.get());
                } else {
                    System.out.println("Error: ROLE_USER not found.");
                }
                member = memberDao.save(member); // 반환되는 객체를 다시 할당
            }

            // SocialMemberDto 저장
            SocialMemberDto newSocialMember = new SocialMemberDto();
            newSocialMember.setSocialProvider(socialProvider);
            newSocialMember.setSocialProviderKey(socialProviderKey);
            newSocialMember.setMemberIdx(member.getIdx()); // member.getIdx()는 이제 올바른 ID를 가집니다.
            socialMemberRepository.save(newSocialMember);
        }
        return new CustomUserDetails(member);
    }

    public List<ChatRoomDto> getChatRoomsForMember(Long memberIdx) {
        // 1. 사용자의 모든 채팅방 조회
        List<ChatRoomDto> chatRooms = chatRoomRepository.findAllByMemberIdx(memberIdx);
        List<ChatRoomDto> chatRoomInfos = new ArrayList<>();

        for (ChatRoomDto room : chatRooms) {
            // 2. 각 채팅방의 마지막 메시지 조회
            Optional<ChatDto> lastMessageOpt = chatRepository.findTopByChatRoomIdxOrderBySendTimeDesc(room.getIdx());

            // 3. 호텔 이름 조회
            String hotelName = hotelRepository.findById(room.getHotelIdx()).map(HotelEntity::getHotelName).orElse("알 수 없는 호텔");

            room.setHotelName(hotelName);

            if (lastMessageOpt.isPresent()) {
                ChatDto lastMessage = lastMessageOpt.get();
                room.setLastMessage(lastMessage.getMessage());
                room.setLastMessageTime(lastMessage.getSendTime().toLocalDateTime());
            } else {
                room.setLastMessage("메시지가 없습니다.");
            }
            chatRoomInfos.add(room);
        }

        // 마지막 메시지 시간 순으로 정렬 (최신이 위로)
        chatRoomInfos.sort(Comparator.comparing(ChatRoomDto::getLastMessageTime, Comparator.nullsLast(Comparator.reverseOrder())));

        return chatRoomInfos;
    }
}