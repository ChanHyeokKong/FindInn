

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
          const row = tbody.insertRow();
          const cell = row.insertCell();
          cell.colSpan = 4; // 테이블 컬럼 개수에 맞춰 colspan 설정
          cell.textContent = "검색 결과가 없습니다.";
        } else {
          data.forEach(hotel => {
            const row = tbody.insertRow();

            row.insertCell().textContent = hotel.hotelIdx;
            row.insertCell().textContent = hotel.hotelEmpty;
            row.insertCell().textContent = hotel.hotelName;
            row.insertCell().textContent = hotel.memberIdx;
          });
        }
      })
      .catch(error => console.error("검색 오류:", error));
  });
  
  const input = document.querySelector("input[name='search']");
  input.addEventListener("keydown",function(event){
	if(event.key ==="Enter"){
		btnSearch.click();
	}
	
  })
  
 
     var today = new Date();
     var dd = today.getDate();
     var mm = today.getMonth()+1; 
  
     var yyyy = today.getFullYear();
     if(dd<10){
       dd='0'+dd
     } 
     if(mm<10){
       mm='0'+mm
     } 
     today = yyyy+'-'+mm+'-'+dd;
  
     document.getElementById("start").setAttribute("min", today);
    
       
	 window.setendmin = function(e) {
		
	     console.log(e);

	     document.getElementById("end").setAttribute("min", e);
	   };
	   
	   window.startDate = function(e){
			
		//입실날짜
			var start=e;
			console.log(start)
			
	   }
	   
	   window.endDate = function(e){
			
		//퇴실날짜
	    	var end=e;  
			console.log(end)
		}
  
	   
	   
  
});