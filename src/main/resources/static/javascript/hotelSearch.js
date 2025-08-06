document.addEventListener("DOMContentLoaded", function() {
	const btnSearch = document.getElementById("btnSearch");

	btnSearch.addEventListener("click", function() {
		const keyword = document.querySelector("input[name='search']").value;

		fetch(`/h_search?keyword=${encodeURIComponent(keyword)}`)
			.then(response => response.json())
			.then(data => {
				const tbody = document.querySelector("#hotelTable tbody");
				tbody.innerHTML = ""; // tbody 초기화

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
              <div class="card" style="display: flex; flex-direction: row; max-height: 200px;">
                <div style="width: 200px; overflow: hidden; display: flex; align-items: center; justify-content: center;">
                  <img src="${hotel.images || '/images/no-image.png'}" alt="호텔 이미지" style="width: 100%; height: auto;" />
                </div>
                <div class="card-body" style="flex: 1;">
                  <h5 class="card-title">${hotel.hotelName}</h5>
                  <p class="card-text">
                    <strong>hotel_idx:</strong> ${hotel.hotelIdx}<br>
                    <strong>empty:</strong> ${hotel.hotelEmpty}<br>
                    <strong>member_idx:</strong> ${hotel.memberIdx}
                  </p>
                </div>
              </div>
            `;

						row.appendChild(cell);
						tbody.appendChild(row);
					});
				}
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


	var today = new Date();
	var dd = today.getDate();
	var mm = today.getMonth() + 1;

	var yyyy = today.getFullYear();
	if (dd < 10) {
		dd = '0' + dd
	}
	if (mm < 10) {
		mm = '0' + mm
	}
	today = yyyy + '-' + mm + '-' + dd;

	document.getElementById("start").setAttribute("min", today);


	window.setendmin = function(e) {

		console.log(e);
		
		
		
		document.getElementById("end").setAttribute("min", e);
	};

	window.startDate = function(e) {

		//입실날짜
		var start = e;
		console.log(start)

	}

	window.endDate = function(e) {

		
		const startDateInput = document.getElementById("start");
		const startValue = startDateInput.value;
		
		if (!startValue) {
		       alert("입실일을 먼저 선택하세요.");
		       
		       document.getElementById("end").value = '';
		       return;
		   }
		
		
		//퇴실날짜
		var end = e;
		console.log(end)
	}

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