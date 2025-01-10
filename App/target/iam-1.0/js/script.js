document.addEventListener('DOMContentLoaded', () => {
    const arrowLink = document.getElementById('arrow-link');
    const signInLink = document.getElementById('goToSignIn');

    if (arrowLink) {
        arrowLink.addEventListener('click', (e) => {
            e.preventDefault();
            window.location.href = 'home.html';
        });
    }

    if (signInLink) {
        signInLink.addEventListener('click', (e) => {
            e.preventDefault();
            window.location.href = 'signin.html';
        });
    }
});