document.addEventListener("DOMContentLoaded", function() {
	// 검색출력
	const btnSearch = document.getElementById("btnSearch");

	btnSearch.addEventListener("click", function() {
		const keyword = document.querySelector('input[name="search"]').value;

		// 라디오 버튼 전부 해제 후 'all' 체크
		const radios = document.querySelectorAll('input[name="category"]');
		radios.forEach(radio => radio.checked = false);
		const allRadio = document.querySelector('input[name="category"][value="all"]');
		if (allRadio) allRadio.checked = true;

		const category = 'all';

		fetch(`/h_search?keyword=${encodeURIComponent(keyword)}&category=${encodeURIComponent(category)}`)
			.then(response => response.json())
			.then(data => {
				renderHotels(data);
			})
			.catch(error => console.error("검색 오류:", error));
	});

	// Enter 키로 검색
	const input = document.querySelector("input[name='search']");
	input.addEventListener("keydown", function(event) {
		if (event.key === "Enter") {
			btnSearch.click();
		}
	});

	// 오늘 이전 날짜 선택 금지
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

	window.startDate = function(e) {
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

	// 사이드바 옵션 radio 이벤트
	const radios = document.querySelectorAll('input[name="category"]');
	radios.forEach(radio => {
		radio.addEventListener('change', () => {
			const keyword = document.querySelector('input[name="search"]').value;
			const selectedCategory = radio.value;

			fetch(`/h_search?keyword=${encodeURIComponent(keyword)}&category=${encodeURIComponent(selectedCategory)}`)
				.then(response => response.json())
				.then(data => {
					renderHotels(data);
				})
				.catch(error => console.error("카테고리 검색 오류:", error));
		});
	});

	// 호텔 목록 렌더링 함수 (중복 제거)
	function renderHotels(data) {
		const tbody = document.querySelector("#hotelTable tbody");
		tbody.innerHTML = ""; // 초기화

		if (data.length === 0) {
			const row = document.createElement("tr");
			const cell = document.createElement("td");
			cell.colSpan = 1;
			cell.textContent = "검색 결과가 없습니다.";
			row.appendChild(cell);
			tbody.appendChild(row);
		} else {
			data.forEach(hotel => {
				const row = document.createElement("tr");
				const cell = document.createElement("td");

				cell.innerHTML = `
					<div class="card hotel-card" data-hotel-id="${hotel.idx}" style="display: flex; flex-direction: row; max-height: 200px;">
						<div style="width: 200px; overflow: hidden; display: flex; align-items: center; justify-content: center;">
							<img src="${hotel.images || '/images/no-image.png'}" alt="호텔 이미지" style="width: 100%; height: auto;" />
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
		}

		// 렌더링 후 호텔 카드 클릭 이벤트 재등록 (이벤트 위임 대신 간단한 재등록)
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

	// 페이지 로드 시 최초 렌더링 혹은 초기 데이터가 있으면 렌더링 및 이벤트 등록 필요할 경우 호출
	// 예: renderHotels(initialData);

});