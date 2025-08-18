package com.inn.service;

import com.inn.data.booking.BookingEntity;
import com.inn.data.booking.BookingRepository;
import com.inn.data.hotel.HotelEntity;
import com.inn.data.hotel.HotelRepository;
import com.inn.data.member.MemberDaoInter;
import com.inn.data.member.MemberDto;
import com.inn.data.review.Review;
import com.inn.data.review.ReviewDto;
import com.inn.data.review.ReviewFile;
import com.inn.data.review.ReviewRepository;
import com.inn.data.rooms.RoomTypes;
import com.inn.data.rooms.RoomTypesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    public RoomsService roomsService;

    @Autowired
    public BookingRepository bookingRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private RoomTypesRepository roomTypesRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private MemberDaoInter memberDaoInter;

    public ReviewDto getOrderData(Long orderId){
        Optional<BookingEntity> data = bookingRepository.findById(orderId);
        ReviewDto dto = new ReviewDto();
        if(data.isPresent()){
            dto.setBookingId(data.get().getIdx());
            Optional<Review> review = reviewRepository.findByBooking(data.get());
            System.out.println("리뷰 확인");
            if (review.isPresent()){
                dto.setIdx(review.get().getIdx());
                dto.setMemberId(review.get().getMember().getIdx());
                dto.setHotelId(review.get().getHotel().getIdx());
                dto.setHotelName(review.get().getHotel().getHotelName());
                dto.setRoomName(review.get().getRoomType().getTypeName());
                dto.setRoomTypeId(review.get().getRoomType().getIdx());
                dto.setReviewDate(review.get().getReviewDate());
                dto.setRating(review.get().getRating());
                dto.setRoomTypeId(review.get().getRoomType().getIdx());
                dto.setHotelId(review.get().getHotel().getIdx());
                dto.setContent(review.get().getContent());
                dto.setReviewId(review.get().getIdx());

                List<ReviewFile> reviewFiles = review.get().getReviewFiles();

                if (reviewFiles != null && !reviewFiles.isEmpty()) {
                    // 3. Map the file entities to a list of their file names only
                    List<String> imageNames = reviewFiles.stream()
                            .map(reviewFile -> Path.of(reviewFile.getStoredFilePath()).getFileName().toString())
                            .collect(Collectors.toList());
                    dto.setImagePaths(imageNames); // Consider renaming this field to imageNames for clarity
                }
                System.out.println(dto);
                return dto;
            }
            dto.setMemberId(data.get().getMemberIdx());
            dto.setReviewDate(LocalDate.now());
            RoomsService.RoomHotelDetailsDto rhd = roomsService.getHotelNamebyRoomTypeId(data.get().getRoomIdx());
            dto.setHotelId(rhd.hotelId());
            dto.setHotelName(rhd.hotelName());
            dto.setRoomName(rhd.roomName());
            dto.setHotelImages(rhd.hotelImages());
            dto.setRoomTypeId(rhd.roomTypeId());
            System.out.println(dto);
            return dto;
        }
        else{
            return null;
        }
    }

    public void save(ReviewDto dto,List<MultipartFile> files) throws IOException {

        if (dto.getRating() == null || dto.getRating() <= 0) {
            throw new IllegalArgumentException("별점을 선택해주세요");
        }

        if (!StringUtils.hasText(dto.getContent()) || dto.getContent().trim().length() < 10) {
            throw new IllegalArgumentException("10글자 이상 작성해주세요");
        }

        Review review = new Review();;
        if (dto.getIdx() !=0) {
            review.setIdx(dto.getIdx());
        }
        Optional<MemberDto> member = memberDaoInter.findById(dto.memberId);
        review.setMember(member.get());
        Optional<HotelEntity> hotel = hotelRepository.findById(dto.getHotelId());
        review.setHotel(hotel.get());
        Optional<RoomTypes> roomTypes = roomTypesRepository.findById(dto.getRoomTypeId());
        review.setRoomType(roomTypes.get());
        review.setReviewDate(LocalDate.now());
        review.setContent(dto.getContent());
        review.setRating(dto.getRating());
        Optional<BookingEntity> booking = bookingRepository.findById(dto.getBookingId());
        if (booking.isPresent()){
            review.setBooking(booking.get());
        }
        if (files != null && !files.isEmpty()) {
            File storageDir = new File(uploadDir);
            if (!storageDir.exists()) {
                storageDir.mkdirs(); // Create directory if it doesn't exist
            }

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // Generate a unique name to prevent overwriting files
                    String originalFileName = file.getOriginalFilename();
                    String storedFileName = UUID.randomUUID() + "_" + originalFileName;
                    String storedFilePath = uploadDir + File.separator + storedFileName;

                    // Save the actual file to the disk
                    file.transferTo(new File(storedFilePath));

                    // Create the metadata entity
                    ReviewFile reviewFile = new ReviewFile();
                    reviewFile.setOriginalFileName(originalFileName);
                    reviewFile.setStoredFilePath(storedFilePath);

                    // Add the file to the review using our helper method
                    review.addReviewFile(reviewFile);
                }
            }
        }

        reviewRepository.save(review);
    }

}
