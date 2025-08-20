document.addEventListener("DOMContentLoaded", function() {

	registerHotelCardClicks();

	
	


	let hotels = []; // 전역변수로 선언

	const sortList = document.getElementById("sortList")
	const btnSearch = document.getElementById("btnSearch");
	const restart = document.querySelector("#restart");
	//초기화 버튼
	restart.addEventListener("click", () => {
	  range.value = 500000;
	  updateDisplay(range.value);

	  // 카테고리 라디오 초기화
	  radios.forEach(radio => radio.checked = false);
	  const allRadio = document.querySelector('input[name="category"][value="all"]');
	  if (allRadio) allRadio.checked = true;

	  // 태그 체크박스 초기화
	  selectedTags = [];
	  checks.forEach(chk => chk.checked = false);

	  // 그리고 검색 실행
	  searchHotels();
	});

	const radios = document.querySelectorAll('input[name="category"]');
	const checks = document.querySelectorAll('input[name="tag"]');
	const input = document.querySelector("input[name='searchKeyword']");
	const tbody = document.querySelector("#hotelTbody");
	const range = document.querySelector('input[name="priceRange"]');
	const display = document.getElementById("priceDisplay");

    // URL 파라미터 추출 함수
    function getUrlParams() {
        const params = new URLSearchParams(window.location.search);
        return {
            keyword: params.get("keyword") || "",
            checkIn: params.get("checkIn") || "",
            checkOut: params.get("checkOut") || "",
            personCount: params.get("personCount") || ""
        };
    }

    // 초기 검색 실행 (URL 파라미터 있으면 반영)
    function initSearchFromParams() {
        const { keyword, checkIn, checkOut, personCount } = getUrlParams();

        let hasParams = keyword || checkIn || checkOut || personCount;

        if (hasParams) {
            // input, date, personCount 에 값 세팅
            if (keyword) document.querySelector("input[name='searchKeyword']").value = keyword;
            if (checkIn) document.getElementById("start").value = checkIn;
            if (checkOut) {
                document.getElementById("end").setAttribute("min", checkIn); // end 날짜 min 동기화
                document.getElementById("end").value = checkOut;
            }
            if (personCount) document.getElementById("personCount").value = personCount;

            // URL 파라미터 기준으로 검색 실행
            searchHotels();
        } else {
            // 기본 검색 실행
            searchHotels();
        }
    }

	//가격
	function updateDisplay(value) {
		const intVal = parseInt(value);
		if (intVal >= 500000) {
			display.textContent = "0원~";
		} else {
			display.textContent = "0원 ~ " + intVal.toLocaleString() + "원";
		}
	}
	
	

	// 최초 로딩 시 값 세팅
	updateDisplay(range.value);

	// 슬라이더 움직일 때 값 갱신
	range.addEventListener("input", function() {
		updateDisplay(this.value);
		searchHotels();  // 슬라이더 조작 시 검색 실행
	});






	let selectedTags = [];

	// 검색 함수 (키워드, 카테고리, 태그 포함)
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

		
		
		
		// tags 파라미터 만들기 (tags=tag1&tags=tag2...)
		const tagParams = selectedTags
			.map(tag => `tags=${encodeURIComponent(tag)}`)
			.join('&');

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
				hotels = data;
				renderHotels(data);
				console.log("검색된 호텔 목록:", hotels);
				displayHotelsOnMap(data);
			})
			.catch(err => console.error("검색 오류:", err));
	}


	//정렬
	sortList.addEventListener("change", () => {
		searchHotels();

	})


	// 검색 버튼 클릭
	btnSearch.addEventListener("click", () => {
		// 검색 시 카테고리 모두 해제 후 'all' 체크
		if (range) range.value = 500000;
		updateDisplay(range.value);
		radios.forEach(radio => radio.checked = false);
		const allRadio = document.querySelector('input[name="category"][value="all"]');
		if (allRadio) allRadio.checked = true;


		// 태그 초기화
		selectedTags = [];
		checks.forEach(chk => chk.checked = false);

		searchHotels();
	});

	// Enter 키로 검색
	input.addEventListener("keydown", function(event) {
		if (event.key === "Enter") btnSearch.click();
	});

	// 카테고리 라디오 변경 시 검색
	radios.forEach(radio => {
		radio.addEventListener("change", () => {
			searchHotels();
		});
	});




	// 태그 체크박스 변경 시 검색
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

	// 호텔 목록 렌더링 함수
	function renderHotels(data) {
        tbody.innerHTML = "";

        if (!data || data.length === 0) {
            const row = document.createElement("tr");
            const cell = document.createElement("td");
            cell.colSpan = 1;
            cell.textContent = "검색 결과가 없습니다.";
            row.appendChild(cell);
            tbody.appendChild(row);
            return;
        }

        data.forEach(hotel => {
            const row = document.createElement("tr");
            const cell = document.createElement("td");

            const imgTag = hotel.hotelImage
                ? `<img src="/hotelImage/${hotel.hotelImage}" alt="호텔 이미지" style="height: 200px; width: 100%; object-fit: cover;" />`
                : "";

            const ratingHtml = hotel.ratingDto
                ? `⭐ ${hotel.ratingDto.rating_avg} (${hotel.ratingDto.rating_count} 리뷰)`
                : `⭐ 0.0 (0 리뷰)`;

            cell.innerHTML = `
            <div class="card hotel-card" data-hotel-id="${hotel.idx}" style="display: flex; flex-direction: row; height: 200px;">
                <!-- 왼쪽 이미지 -->
                <div style="width: 200px; overflow: hidden; display: flex; align-items: center; justify-content: center;">
                    ${imgTag}
                </div>

                <!-- 오른쪽 정보 -->
                <div class="card-body" style="flex: 1; display: flex; flex-direction: column; justify-content: space-between; padding: 15px;">
                    <h5 class="card-title">${hotel.hotelName}</h5>
                    <p class="card-text" style="margin: 5px 0;">${hotel.hotelAddress}</p>
                    <p class="card-text" style="margin: 5px 0;">${ratingHtml}</p>
                    <div style="display: flex; justify-content: flex-end; align-items: flex-end; flex-grow: 1;">
                        <span style="font-weight: bold; font-size: 18px; color: #007bff;">
                            ₩ ${hotel.priceRange.toLocaleString()}
                        </span>
                    </div>
                </div>
            </div>
            `;

            row.appendChild(cell);
            tbody.appendChild(row);
        });

        registerHotelCardClicks();
    }


	// 호텔 카드 클릭 이벤트 등록 함수
	function registerHotelCardClicks() {
		const cards = document.querySelectorAll('.hotel-card');

		cards.forEach(card => {
			card.onclick = () => {
				const hotelId = card.getAttribute('data-hotel-id');

				const checkIn = document.getElementById('start').value;
				const checkOut = document.getElementById('end').value;
				const personCount = document.getElementById('personCount').value;

				
 				const url = `domestic-accommodations?id=${hotelId}&checkIn=${encodeURIComponent(checkIn || '')}&checkOut=${encodeURIComponent(checkOut || '')}&personCount=${encodeURIComponent(personCount || '')}`;				location.href = url;
			};
		});
	}

	// 날짜 관련 설정
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

	let checkIn = '';
	let checkOut = '';
	window.startDate = function(e) {
		checkIn = e;
		console.log("입실날짜:", e);
	};

	window.endDate = function(e) {
		const startDateInput = document.getElementById("start");
		const startValue = startDateInput.value;

		if (!startValue) {
			alert("입실일을 먼저 선택하세요.");
			document.getElementById("end").value = '';
			return;
		}
		checkOut = e;
		console.log("퇴실날짜:", e);
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
			// script.onload 시점에서 kakao.maps.services가 아직 로드 안됐을 수도 있음.
			// 따라서 잠시 딜레이 후 callback 호출
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

	loadKakaoMapScript(() => {
		// hotels 배열이 비어있으면 마커 표시 안 됨.
		if (hotels.length === 0) {
			console.log("호텔 데이터가 없습니다.");
			return;
		}
		displayHotelsOnMap(hotels);
	});
	
	let map = null;               // 지도 전역 변수
	let markers = [];             // 마커 저장소

	function clearMarkers() {
	  markers.forEach(marker => marker.setMap(null));
	  markers = [];
	}
	
	
	
	
	// 호텔 마커 지도에 표시
	function displayHotelsOnMap(hotels) {
		if (typeof kakao === 'undefined' || !kakao.maps) {
			console.error("Kakao 지도 API가 로드되지 않았습니다.");
			return;
		}
		
		if (!map) {
		    // 최초 맵 생성 로직
		  } else {
		    clearMarkers(); // 기존 마커 제거
		  }
		  
		  if (!hotels || hotels.length === 0) {
		     console.log("호텔 데이터가 없습니다.");
		     return;  // 데이터 없으면 마커 표시하지 않고 종료
		   }

		const mapContainer = document.getElementById('map');
		const geocoder = new kakao.maps.services.Geocoder();
		const firstHotelAddress = hotels[0].hotelAddress;

		geocoder.addressSearch(firstHotelAddress, function(result, status) {
			if (status === kakao.maps.services.Status.OK) {
				const lat = result[0].y;
				const lng = result[0].x;

				const mapOption = {
					center: new kakao.maps.LatLng(lat, lng), // 첫 호텔 위치를 중심으로
					level: 13
				};

				 map = new kakao.maps.Map(mapContainer, mapOption);

				// 호텔 데이터에 따라 마커 표시
				hotels.forEach(hotel => {
					const address = hotel.hotelAddress;
					console.log(address);
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
							content.style.cursor = 'pointer';  // 마우스 올렸을 때 포인터로 보이게

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
							  <img src="/hotelImage/${hotel.hotelImage}" alt="호텔 이미지" style="width: 100%; max-height: 100px; object-fit: cover; margin-top: 5px;">	
							    <strong>${hotel.hotelName}</strong><br>
							    가격: ${hotel.priceRange.toLocaleString()}원<br>
							    주소: ${hotel.hotelAddress}<br>
							    전화: ${hotel.hotelTel}<br>
							    
							  `;

							  // content 요소 기준으로 위치 조정 (마우스 위치가 아닌 마커 위치 기준)
							  content.appendChild(infoCard);
							});

							content.addEventListener('click', ()=>{
								
								const hotelId = content.getAttribute('data-hotel-id', hotel.idx);
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
			}
		});
	}
	
	

	// 모달 열릴 때마다 호텔 표시
	const mapModal = document.getElementById('mapModal');
	mapModal.addEventListener('shown.bs.modal', () => {
		
		loadKakaoMapScript(() => {
			displayHotelsOnMap(hotels);
			
			
		});
	});







	initSearchFromParams();
});