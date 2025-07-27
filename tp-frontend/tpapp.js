// Initialize the Elm application
document.addEventListener('DOMContentLoaded', function() {
    const app = Elm.UI.Main.init({
        node: document.getElementById('app'),
        flags: {}
    });
    
    console.log('ThirdPlace app initialized');
});