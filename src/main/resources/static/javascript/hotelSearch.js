document.addEventListener("DOMContentLoaded", function() {
	
	registerHotelCardClicks();

	const btnSearch = document.getElementById("btnSearch");
	if (btnSearch) {
	    btnSearch.addEventListener("click", function() {
	        // 이벤트 핸들러 내용
	    });
	} else {
	    console.error("btnSearch 요소가 존재하지 않습니다.");
	}
	
	const radios = document.querySelectorAll('input[name="category"]');
	const checks = document.querySelectorAll('input[name="tag"]');
	const input = document.querySelector("input[name='searchKeyword']");
	const tbody = document.querySelector("#hotelTbody");

	let selectedTags = [];

	// 검색 함수 (키워드, 카테고리, 태그 포함)
	function searchHotels() {
		const keyword = input.value.trim();
		const categoryRadio = Array.from(radios).find(r => r.checked);
		const category = categoryRadio ? categoryRadio.value : 'all';

		// tags 파라미터 만들기 (tags=tag1&tags=tag2...)
		const tagParams = selectedTags		
		    .map(tag => `tags=${encodeURIComponent(tag)}`)
		    .join('&');
		console.log(checkIn);

		let url = `/h_search?keyword=${encodeURIComponent(keyword)}&category=${encodeURIComponent(category)}&checkIn=${encodeURIComponent(checkIn)}&checkOut=${encodeURIComponent(checkOut)}`;
		if (tagParams) url += `&${tagParams}`;
		
		console.log("완성된 URL:", url);


		fetch(url)
			.then(res => res.json())
			.then(data => {
				renderHotels(data);
			})
			.catch(err => console.error("검색 오류:", err));
	}

	// 검색 버튼 클릭
	btnSearch.addEventListener("click", () => {
		// 검색 시 카테고리 모두 해제 후 'all' 체크
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
			const row = document.createElement("tr");
			const cell = document.createElement("td");

			let imgTag = "";
			if (hotel.hotelImages && hotel.hotelImages.length > 0) {
				imgTag = `<img src="/hotelImage/${hotel.hotelImages[0]}" alt="호텔 이미지" style="height: 200px; display: block;" />`;
			}

			cell.innerHTML = `
				<div class="card hotel-card" data-hotel-id="${hotel.idx}" style="display: flex; flex-direction: row; height: 200px;">
					<div style="width: 200px; overflow: hidden; display: flex; align-items: center; justify-content: center;">
						${imgTag}
					</div>
					<div class="card-body" style="flex: 1;">
						<h5 class="card-title">${hotel.hotelName}</h5>
						<p class="card-text">
							<strong>hotel_idx:</strong> ${hotel.idx}<br>
							<strong>member_idx:</strong> ${hotel.memberIdx}
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
				const personal = document.getElementById('capacity').value;

				if (!checkIn || !checkOut || !personal) {
					alert("날짜, 인원수를 모두 입력해주세요.");
					return;
				}

				const url = `domestic-accommodations?id=${hotelId}&checkIn=${encodeURIComponent(checkIn)}&checkOut=${encodeURIComponent(checkOut)}&personal=${encodeURIComponent(personal)}`;
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

	// 맵 모달
	const mapModal = document.getElementById('mapModal');
	if (mapModal) {
		mapModal.addEventListener('shown.bs.modal', function() {
			if (typeof kakao === 'undefined' || !kakao.maps) {
				console.error("Kakao 지도 API가 아직 로드되지 않았습니다.");
				return;
			}

			const mapContainer = document.getElementById('map');
			if (mapContainer) {
				const mapOption = {
					center: new kakao.maps.LatLng(33.450701, 126.570667),
					level: 3
				};

				const map = new kakao.maps.Map(mapContainer, mapOption);

				// 지도 크기 재설정 (모달 안에 있을 때 꼭 필요함)
				setTimeout(() => map.relayout(), 100);
			}
		});
	}
});