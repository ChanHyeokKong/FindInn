document.addEventListener('DOMContentLoaded', () => {
  const input = document.getElementById('guestPhone');

  // 백스페이스 키 처리용 플래그
  let isBackspace = false;

  input.addEventListener('keydown', (e) => {
    isBackspace = e.key === 'Backspace';

    if (isBackspace) {
      const cursorPos = input.selectionStart;
      // 커서 바로 앞 문자가 하이픈이면
      if (cursorPos > 0 && input.value[cursorPos - 1] === '-') {
        e.preventDefault(); // 기본 백스페이스 막기

        // 숫자만 추출
        let numbers = input.value.replace(/\D/g, '');

        // 커서 위치 기준 하이픈 제외한 숫자 위치 계산
        // 하이픈 앞에 커서면, 숫자 index는 cursorPos - 하이픈 개수 앞부분
        let hyphensBefore = (input.value.slice(0, cursorPos).match(/-/g) || []).length;
        let numberIndex = cursorPos - hyphensBefore - 1;  // 지울 숫자 인덱스

        // 한 글자 삭제
        if (numberIndex >= 0) {
          numbers = numbers.slice(0, numberIndex) + numbers.slice(numberIndex + 1);
        }

        // 다시 하이픈 넣기
        let formatted = '';
        if (numbers.length < 4) {
          formatted = numbers;
        } else if (numbers.length < 7) {
          formatted = numbers.slice(0, 3) + '-' + numbers.slice(3);
        } else {
          formatted = numbers.slice(0, 3) + '-' + numbers.slice(3, 7) + '-' + numbers.slice(7);
        }

        input.value = formatted;

        // 커서 위치 다시 맞추기 (하이픈 넣은 위치에 따라 계산)
        let newCursorPos = cursorPos - 1;
        if (newCursorPos < 0) newCursorPos = 0;
        input.setSelectionRange(newCursorPos, newCursorPos);
      }
    }
  });

  input.addEventListener('input', () => {
    if (isBackspace) {
      // 백스페이스 이벤트가 input 전에 발생하므로, 여기선 별도 처리 안 함
      isBackspace = false;
      return;
    }

    const value = input.value;
    let numbers = value.replace(/\D/g, '');

    if (numbers.length > 11) {
      numbers = numbers.slice(0, 11);
    }

    let formatted = '';
    if (numbers.length < 4) {
      formatted = numbers;
    } else if (numbers.length < 7) {
      formatted = numbers.slice(0, 3) + '-' + numbers.slice(3);
    } else {
      formatted = numbers.slice(0, 3) + '-' + numbers.slice(3, 7) + '-' + numbers.slice(7);
    }

    // 커서 위치 보정 없이 값만 세팅
    input.value = formatted;
  });
});
