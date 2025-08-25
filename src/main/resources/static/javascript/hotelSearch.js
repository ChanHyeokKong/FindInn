document.addEventListener("DOMContentLoaded", function() {
    console.log('HotelSearch.js loaded successfully');

    let hotels = Array.isArray(window.initialHotels) ? window.initialHotels : []; 

    const sortList = document.getElementById("sortList")
    const btnSearch = document.getElementById("btnSearch");
    const restart = document.querySelector("#restart");
    const filterToggle = document.getElementById('filterToggle');
    const filterSidebar = document.getElementById('filterSidebar');
                
    filterToggle.addEventListener('click', function() {
        console.log('Filter toggle clicked');
        const isHidden = filterSidebar.style.display === 'none';
        filterSidebar.style.display = isHidden ? 'block' : 'none';
        
        if (isHidden) {
            this.innerHTML = '<i class="fas fa-times me-2"></i>필터 닫기';
        } else {
            this.innerHTML = '<i class="fas fa-filter me-2"></i>필터';
        }
    });
    
    restart.addEventListener("click", () => {
      range.value = 500000;
      updateDisplay(range.value);
      radios.forEach(radio => radio.checked = false);
      const allRadio = document.querySelector('input[name="category"][value="all"]');
      if (allRadio) allRadio.checked = true;
      selectedTags = [];
      checks.forEach(chk => chk.checked = false);
      searchHotels();
    });

    const radios = document.querySelectorAll('input[name="category"]');
    const checks = document.querySelectorAll('input[name="tag"]');
    const input = document.querySelector("#searchKeyword");
    const hotelContainer = document.querySelector("#hotelContainer");
    const range = document.querySelector('input[name="priceRange"]');
    const display = document.getElementById("priceDisplay");

    function getUrlParams() {
        const params = new URLSearchParams(window.location.search);
        return {
            keyword: params.get("keyword") || "",
            checkIn: params.get("checkIn") || "",
            checkOut: params.get("checkOut") || "",
            personCount: params.get("personCount") || ""
        };
    }

    function initSearchFromParams() {
        const { keyword, checkIn, checkOut, personCount } = getUrlParams();
        let hasParams = keyword || checkIn || checkOut || personCount;

        if (hasParams) {
            if (keyword) document.querySelector("#searchKeyword").value = keyword;
            if (checkIn) document.getElementById("start").value = checkIn;
            if (checkOut) {
                document.getElementById("end").setAttribute("min", checkIn);
                document.getElementById("end").value = checkOut;
            }
            if (personCount) document.getElementById("personCount").value = personCount;
            searchHotels();
        } else {
            console.log('No search parameters, using server-provided hotel list');
            renderHotels(hotels);
        }
    }

    function updateDisplay(value) {
        const intVal = parseInt(value);
        if (intVal >= 500000) {
            display.textContent = "0원~";
        } else {
            display.textContent = "0원 ~ " + intVal.toLocaleString() + "원";
        }
    }
    
    updateDisplay(range.value);
    range.addEventListener("input", function() {
        updateDisplay(this.value);
        searchHotels();
    });

    let selectedTags = [];

    function searchHotels() {
        const keyword = input.value.trim();
        const categoryRadio = Array.from(radios).find(r => r.checked);
        const category = categoryRadio ? categoryRadio.value : 'all';
        const priceRangeInput = document.querySelector('input[name="priceRange"]');
        const priceRange = priceRangeInput ? priceRangeInput.value : '';
        const checkIn = document.getElementById("start").value;
        const checkOut = document.getElementById("end").value;
        const personCount = document.getElementById("personCount").value;
        const sort = document.getElementById("sortList").value;

        const params = new URLSearchParams();
        if (keyword) params.append("keyword", keyword);
        if (category && category !== "all") params.append("category", category);
        if (checkIn) params.append("checkIn", checkIn);
        if (checkOut) params.append("checkOut", checkOut);
        if (priceRange) params.append("priceRange", priceRange);
        if (personCount) params.append("personCount", personCount);
        if (sort) params.append("sort", sort);
        
        selectedTags.forEach(tag => params.append("tags", tag));
        const url = "/h_search?" + params.toString();

        console.log("완성된 URL:", url);
        fetch(url, { cache: 'no-cache' })
            .then(res => res.json())
            .then(data => {
				console.log("서버 응답 데이터:", data);
                hotels = data;
                renderHotels(data);
                console.log("검색된 호텔 목록:", hotels);
                displayHotelsOnMap(data);
            })
            .catch(err => console.error("검색 오류:", err));
    }

    sortList.addEventListener("change", () => {
        searchHotels();
    })

    btnSearch.addEventListener("click", () => {
        searchHotels();
    });

    input.addEventListener("keydown", function(event) {
        if (event.key === "Enter") btnSearch.click();
    });

    radios.forEach(radio => {
        radio.addEventListener("change", () => {
            searchHotels();
        });
    });

    checks.forEach(check => {
        check.addEventListener("change", () => {
            selectedTags = Array.from(checks)
                .filter(chk => chk.checked)
                .map(chk => chk.value);
            selectedTags = [...new Set(selectedTags)];
            console.log("선택된 태그들:", selectedTags);
            searchHotels();
        });
    });

    function renderHotels(data) {
        hotelContainer.innerHTML = "";
        const hotelCountElement = document.getElementById('hotelCount');
        if (hotelCountElement) {
            hotelCountElement.textContent = data ? data.length : 0;
        }

        if (!data || data.length === 0) {
            const emptyDiv = document.createElement("div");
            emptyDiv.className = "col-12 text-center py-5";
            emptyDiv.innerHTML = `
                <div class="mb-4">
                    <i class="fas fa-search fa-4x text-muted"></i>
                </div>
                <h4 class="text-muted">검색 결과가 없습니다</h4>
                <p class="text-muted">다른 검색 조건으로 다시 시도해보세요.</p>
            `;
            hotelContainer.appendChild(emptyDiv);
            return;
        }

        data.forEach(hotel => {
            const colDiv = document.createElement("div");
            colDiv.className = "col-12";
            const imgTag = hotel.hotelImage
                ? `<img src="/uploads/hotels/${hotel.hotelImage}" alt="호텔 이미지" loading="lazy" />`
                : `<div class="d-flex align-items-center justify-content-center bg-light h-100">
                    <i class="fas fa-hotel fa-3x text-muted"></i>
                   </div>`;

            const hotelRating = hotel.ratingDto ? hotel.ratingDto.rating_avg : 0;
            const formattedRating = hotelRating.toFixed(1);
            let starsHtml = '';
            const fullStars = Math.floor(hotelRating); 
            const hasHalfStar = (hotelRating % 1) >= 0.5;
            for (let i = 0; i < fullStars; i++) {
                starsHtml += `<i class="fas fa-star text-warning"></i>`;
            }
            if (hasHalfStar) {
                starsHtml += `<i class="fas fa-star-half-alt text-warning"></i>`;
            }
            const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
            for (let i = 0; i < emptyStars; i++) {
                starsHtml += `<i class="far fa-star text-muted"></i>`;
            }

            const ratingText = hotel.ratingDto 
                ? `<span class="fw-semibold me-2">${formattedRating}</span>
                   <span class="text-muted">(${hotel.ratingDto.rating_count} 리뷰)</span>`
                : `<span class="text-muted">(리뷰 없음)</span>`;

            colDiv.innerHTML = `
                <div class="hotel-card" data-hotel-id="${hotel.idx}">
                    <div class="d-flex">
                        <div class="hotel-image">
                            ${imgTag}
                        </div>
                        <div class="hotel-info">
                            <div>
                                <h3 class="hotel-title">${hotel.hotelName}</h3>
                                <div class="hotel-address">
                                    <i class="fas fa-map-marker-alt text-primary me-2"></i>
                                    <span>${hotel.hotelAddress}</span>
                                </div>
                                <div class="hotel-rating">
                                    <div class="rating-stars me-2">
                                        ${starsHtml}
                                    </div>
                                    ${ratingText}
                                </div>
                                <div class="hotel-features">
                                    <span class="feature-badge">
                                        <i class="fas fa-wifi me-1"></i>무료 Wi-Fi
                                    </span>
                                    <span class="feature-badge">
                                        <i class="fas fa-car me-1"></i>주차 가능
                                    </span>
                                    <span class="feature-badge">
                                        <i class="fas fa-concierge-bell me-1"></i>24시간 서비스
                                    </span>
                                </div>
                            </div>
                            <div class="hotel-bottom">
                                <div class="d-flex align-items-center">
                                    <button class="btn btn-outline-primary btn-sm me-2" onclick="event.stopPropagation();">
                                        <i class="fas fa-heart me-1"></i>찜하기
                                    </button>
                                    <button class="btn btn-outline-secondary btn-sm" onclick="event.stopPropagation();">
                                        <i class="fas fa-share me-1"></i>공유
                                    </button>
                                </div>
                                <div class="price-tag">
                                    <div class="text-end">
                                        <small class="d-block opacity-75">1박 기준</small>
                                        <strong>₩${Number(hotel.priceRange).toLocaleString()}</strong>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            `;
            hotelContainer.appendChild(colDiv);
        });
        registerHotelCardClicks();
    }

    function registerHotelCardClicks() {
        const cards = document.querySelectorAll('.hotel-card');
        cards.forEach((card) => {
            card.onclick = () => {
                const hotelId = card.getAttribute('data-hotel-id');
                const checkIn = document.getElementById('start').value;
                const checkOut = document.getElementById('end').value;
                const personCount = document.getElementById('personCount').value;
                const url = `domestic-accommodations?id=${hotelId}&checkIn=${encodeURIComponent(checkIn || '')}&checkOut=${encodeURIComponent(checkOut || '')}&personCount=${encodeURIComponent(personCount || '')}`;
                location.href = url;
            };
        });
    }

    var today = new Date();
    var dd = today.getDate();
    var mm = today.getMonth() + 1;
    var yyyy = today.getFullYear();

    if (dd < 10) dd = '0' + dd;
    if (mm < 10) mm = '0' + mm;
    today = yyyy + '-' + mm + '-' + dd;
    document.getElementById("start").setAttribute("min", today);

    window.setendmin = function(e) {
        document.getElementById("end").setAttribute("min", e);
        document.getElementById("end").value = "";
    };

    window.startDate = function(e) {
        console.log("입실날짜:", e);
    };

    window.endDate = function(e) {
        const startDateInput = document.getElementById("start");
        const startValue = startDateInput.value;
        if (!startValue) {
            alert("입실일을 먼저 선택하세요.");
            document.getElementById("end").value = '';
        } else {
            console.log("퇴실날짜:", e);
        }
    };

    function loadKakaoMapScript(callback) {
        const isLoaded = window.kakao && window.kakao.maps && window.kakao.maps.services;
        if (isLoaded) {
            callback();
            return;
        }

        const script = document.createElement('script');
        script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=5ac2ea2e11f7b380cdf52afbcc384b44&libraries=services`;
        script.onload = () => {
            setTimeout(() => {
                if (window.kakao && window.kakao.maps && window.kakao.maps.services) {
                    callback();
                } else {
                    console.error("Kakao Maps services 라이브러리가 정상 로드되지 않았습니다.");
                }
            }, 100);
        };
        document.head.appendChild(script);
    }
    
    let map = null;               
    let markers = [];             

    function clearMarkers() {
      markers.forEach(marker => marker.setMap(null));
      markers = [];
    }
    
    function displayHotelsOnMap(hotels) {
        if (typeof kakao === 'undefined' || !kakao.maps) {
            console.error("Kakao 지도 API가 로드되지 않았습니다.");
            return;
        }
        
        if (!hotels || hotels.length === 0) {
            console.log("호텔 데이터가 없습니다. 기본 지도 표시");
            const mapContainer = document.getElementById('map');
            const mapOption = {
                center: new kakao.maps.LatLng(37.5665, 126.9780),
                level: 7
            };
            if (!map) map = new kakao.maps.Map(mapContainer, mapOption); else map.setCenter(mapOption.center);
            clearMarkers();
            return;
        }

        const mapContainer = document.getElementById('map');
        const geocoder = new kakao.maps.services.Geocoder();
        const firstHotelAddress = hotels[0].hotelAddress;

        geocoder.addressSearch(firstHotelAddress, function(result, status) {
            let centerLatLng;
            if (status === kakao.maps.services.Status.OK && result && result[0]) {
                const lat = result[0].y;
                const lng = result[0].x;
                centerLatLng = new kakao.maps.LatLng(lat, lng);
            } else {
                centerLatLng = new kakao.maps.LatLng(37.5665, 126.9780); 
            }
             
            if (!map) {
                const mapOption = { center: centerLatLng, level: 10 };
                map = new kakao.maps.Map(mapContainer, mapOption);
            } else {
                map.setLevel(10);
                map.setCenter(centerLatLng);
                clearMarkers();
            }

            hotels.forEach(hotel => {
                const address = hotel.hotelAddress;
                geocoder.addressSearch(address, function(result, status) {
                    if (status === kakao.maps.services.Status.OK) {
                        const lat = result[0].y;
                        const lng = result[0].x;
                        const content = document.createElement('div');
                        content.style.padding = '5px';
                        content.style.background = 'white';
                        content.style.border = '1px solid #ccc';
                        content.style.borderRadius = '5px';
                        content.style.fontSize = '12px';
                        content.style.cursor = 'pointer';
                        content.textContent = `${hotel.priceRange.toLocaleString()}원`;
                        content.setAttribute('data-hotel-id', hotel.idx);
                        
                        const position = new kakao.maps.LatLng(lat, lng);
                        const customOverlay = new kakao.maps.CustomOverlay({
                            position: position,
                            content: content,
                            yAnchor: 1
                        });
                        customOverlay.setMap(map);
                        markers.push(customOverlay);
                        
                        let infoCard = null;
                        content.addEventListener('mouseenter', () => {
                          if (infoCard) return;
                          infoCard = document.createElement('div');
                          infoCard.style.position = 'absolute';
                          infoCard.style.minWidth = '200px';
                          infoCard.style.padding = '10px';
                          infoCard.style.background = 'white';
                          infoCard.style.border = '1px solid #999';
                          infoCard.style.borderRadius = '8px';
                          infoCard.style.boxShadow = '0 2px 6px rgba(0,0,0,0.3)';
                          infoCard.style.fontSize = '12px';
                          infoCard.style.zIndex = 2000;
                          infoCard.innerHTML = `
                          <img src="/uploads/hotels/${hotel.hotelImage}" alt="호텔 이미지" style="max-width: 100%; max-height: 100px; object-fit: cover; margin-top: 5px;">	
                            <strong>${hotel.hotelName}</strong><br>
                            가격: ${hotel.priceRange.toLocaleString()}원<br>
                            주소: ${hotel.hotelAddress}<br>
                            전화: ${hotel.hotelTel}<br>
                          `;
                          content.appendChild(infoCard);
                        });

                        content.addEventListener('click', ()=>{
                            const hotelId = content.getAttribute('data-hotel-id');
                            const checkIn = document.getElementById('start').value;
                            const checkOut = document.getElementById('end').value;
                            const personCount = document.getElementById('personCount').value;
                            location.href=`domestic-accommodations?id=${hotelId}&checkIn=${encodeURIComponent(checkIn)}&checkOut=${encodeURIComponent(checkOut)}&personCount=${encodeURIComponent(personCount)}`
                        })
                        
                        content.addEventListener('mouseleave', () => {
                          if (infoCard) {
                            infoCard.remove();
                            infoCard = null;
                          }
                        })
                    }
                });
            });
        });
    }
    
    const mapModal = document.getElementById('mapModal');
    mapModal.addEventListener('shown.bs.modal', () => {
        console.log('[MapModal] shown event fired');
        loadKakaoMapScript(() => {
            const seedHotels = (Array.isArray(hotels) && hotels.length > 0)
                ? hotels
                : (Array.isArray(window.initialHotels) ? window.initialHotels : []);
            console.log('[MapModal] rendering map with hotels:', seedHotels.length);
            displayHotelsOnMap(seedHotels);
            setTimeout(() => {
                try {
                    if (map) {
                        kakao.maps.event.trigger(map, 'resize');
                        const center = map.getCenter() || new kakao.maps.LatLng(37.5665, 126.9780);
                        map.setCenter(center);
                    }
                } catch (e) {
                    console.warn('[MapModal] resize failed', e);
                }
            }, 500); // 딜레이를 500ms로 늘림
        });
    });

    initSearchFromParams();
});

function goToHotelDetail(element) {
    const hotelId = element.getAttribute('data-hotel-id');
    const checkIn = document.getElementById('start').value;
    const checkOut = document.getElementById('end').value;
    const personCount = document.getElementById('personCount').value;
    
    const url = `domestic-accommodations?id=${hotelId}&checkIn=${encodeURIComponent(checkIn || '')}&checkOut=${encodeURIComponent(checkOut || '')}&personCount=${encodeURIComponent(personCount || '')}`;
    location.href = url;
}