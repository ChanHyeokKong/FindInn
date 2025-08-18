document.addEventListener("DOMContentLoaded", function() {

	registerHotelCardClicks();



	let hotels = []; // 전역변수로 선언

	const sortList = document.getElementById("sortList")
	const btnSearch = document.getElementById("btnSearch");
	const restart = document.querySelector("#restart");
	//초기화 버튼
	restart.addEventListener("click", () => {
		location.reload();
	})

	const radios = document.querySelectorAll('input[name="category"]');
	const checks = document.querySelectorAll('input[name="tag"]');
	const input = document.querySelector("input[name='searchKeyword']");
	const tbody = document.querySelector("#hotelTbody");
	const range = document.querySelector('input[name=priceRange]');
	const display = document.getElementById("priceDisplay");

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



		// tags 파라미터 만들기 (tags=tag1&tags=tag2...)
		const tagParams = selectedTags
			.map(tag => `tags=${encodeURIComponent(tag)}`)
			.join('&');


		let url = `/h_search?keyword=${encodeURIComponent(keyword)}&category=${encodeURIComponent(category)}&checkIn=${encodeURIComponent(checkIn)}&checkOut=${encodeURIComponent(checkOut)}
		&priceRange=${encodeURIComponent(priceRange)}&personCount=${encodeURIComponent(personCount)}&sort=${encodeURIComponent(sort)}`;
		if (tagParams) url += `&${tagParams}`;

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

		if (data.length === 0) {
			const row = document.createElement("tr");
			const cell = document.createElement("td");
			cell.colSpan = 1;
			cell.textContent = "검색 결과가 없습니다.";
			row.appendChild(cell);
			tbody.appendChild(row);
			return;
		}

		data.forEach(hotel => {

			console.log("hotel.idx:", hotel.idx, "hotelImage:", hotel.hotelImage);
			let imgTag = "";
			if (hotel.hotelImage) {
				imgTag = `<img src="/hotelImage/${hotel.hotelImage}" alt="호텔 이미지" style="height: 200px; display: block;" />`;
			} else {
				console.log(`호텔 idx ${hotel.idx}에 이미지 없음`);
			}

			const row = document.createElement("tr");
			const cell = document.createElement("td");

			//let imgTag = "";
			if (hotel.hotelImage) {
				imgTag = `<img src="/hotelImage/${hotel.hotelImage}" alt="호텔 이미지" style="height: 200px; display: block;" />`;
			}


			cell.innerHTML = `
			  <div class="card hotel-card" data-hotel-id="${hotel.idx}">
			    <div class="image-container">
			      ${imgTag}
			    </div>
				<div class="card-body" style="flex: 1;">
				              <h5 class="card-title">${hotel.hotelName}</h5>
				              <p class="card-text">
				                <strong>hotel_idx:</strong> <span>${hotel.idx}</span><br>
				                <strong>member_idx:</strong> <span>${hotel.memberIdx}</span><br>
				                <strong>min_price:</strong> <span>${hotel.priceRange}</span><br>
				                <strong>address:</strong> <span>${hotel.hotelAddress}</span><br>
				                <strong>Tel:</strong> <span>${hotel.hotelTel}</span><br>
				              </p>
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

				if (!checkIn || !checkOut || !personCount) {
					alert("날짜, 인원수를 모두 입력해주세요.");
					return;
				}

				const url = `domestic-accommodations?id=${hotelId}&checkIn=${encodeURIComponent(checkIn)}&checkOut=${encodeURIComponent(checkOut)}&personCount=${encodeURIComponent(personCount)}`;
				location.href = url;
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

	// 호텔 마커 지도에 표시
	function displayHotelsOnMap(hotels) {
		if (typeof kakao === 'undefined' || !kakao.maps) {
			console.error("Kakao 지도 API가 로드되지 않았습니다.");
			return;
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
					level: 10
				};

				const map = new kakao.maps.Map(mapContainer, mapOption);

				// 호텔 데이터에 따라 마커 표시
				hotels.forEach(hotel => {
					const address = hotel.hotelAddress;
					console.log(address);
					geocoder.addressSearch(address, function(result, status) {
						if (status === kakao.maps.services.Status.OK) {
							const lat = result[0].y;
							const lng = result[0].x;
							const content = `
	                    <div style="padding:5px; background:white; border:1px solid #ccc; border-radius:5px; font-size:12px;">
	                      ${hotel.priceRange}원
	                        
	                    </div>`;

							const position = new kakao.maps.LatLng(lat, lng);
							const customOverlay = new kakao.maps.CustomOverlay({
								position: position,
								content: content,
								yAnchor: 1
							});

							customOverlay.setMap(map);
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







	searchHotels();
});