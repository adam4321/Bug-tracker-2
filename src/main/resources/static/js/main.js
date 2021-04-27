/*************************************************************
**  Description: MAIN layout - client-side JavaScript file
**************************************************************/

/* CREATE PAGE MODALS ------------------------------------------------------ */

// Get the modal
let aboutModal = document.getElementById("aboutModal");
let contactModal = document.getElementById("contactModal");
let infoModal = document.getElementById("infoModal");

// Get the button that opens the modal
let aboutBtn = document.getElementById("aboutBtn");
let contactBtn = document.getElementById("contactBtn");
let infoBtn = document.getElementById("infoBtn");

// Get the <span> element that closes the modal
let aboutSpan = document.getElementsByClassName("aboutClose")[0];
let contactSpan = document.getElementsByClassName("contactClose")[0];
let infoSpan = document.getElementsByClassName("infoClose")[0];

// When the user clicks on the button, open the modal
aboutBtn.onclick = () => { aboutModal.style.display = "block"; }
contactBtn.onclick = () => { contactModal.style.display = "block"; }
infoBtn.onclick = () => { infoModal.style.display = "block"; }

// When the user clicks on <span> (x), close the modal
aboutSpan.onclick = () => { aboutModal.style.display = "none"; }
contactSpan.onclick = () => { contactModal.style.display = "none"; }
infoSpan.onclick = () => { infoModal.style.display = "none"; }

// When the user clicks anywhere outside of the modal, close it
window.onclick = (event) => {
    if (event.target == aboutModal) {
        aboutModal.style.display = "none";
    }
    if (event.target == contactModal) {
        contactModal.style.display = "none";
    }
    if (event.target == infoModal) {
        infoModal.style.display = "none";
    }
}


/* POST request to log out the user ---------------------------------------- */

let logout = () => {
    let req = new XMLHttpRequest();
    let path = '/bug_tracker/logout';

    // Ajax request
    req.open('POST', path, true);
    req.setRequestHeader('Content-Type', 'application/json');
    req.setRequestHeader("X-XSRF-TOKEN", Cookies.get('XSRF-TOKEN'));
    req.addEventListener('load', () => {
        if (req.status >= 200 && req.status < 400) {
            // Redirect to companies page
            window.location.href = "/bug_tracker/login";
        } 
        else {
            console.error('Database return error');
        }
    });

    req.send();
}
