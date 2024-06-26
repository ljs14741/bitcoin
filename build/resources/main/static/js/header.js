function toggleMenu() {
    const nav = document.querySelector('header .main-nav ul');
    nav.classList.toggle('active');
}

function redirectToKakaoLogin(event) {
    event.preventDefault();
    const currentUrl = encodeURIComponent(window.location.href);
    window.location.href = `/oauth/kakao?state=${currentUrl}`;
}