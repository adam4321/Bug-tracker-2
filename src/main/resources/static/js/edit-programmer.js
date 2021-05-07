/******************************************************************
**  Description: UPDATE PROGRAMMER - Client-side JavaScript file
******************************************************************/

let recordForm = document.getElementById('recordForm');
let spinner = document.getElementById('spinner');
spinner.style.visibility = "hidden";


// Add event listener to prevent default action of "save" button
function submitUpdate() {
    spinner.style.visibility = "visible";
    let req = new XMLHttpRequest();
    const PATH = "/bug_tracker/edit_programmer/updateProgrammer";
 
    const urlParams = new URLSearchParams(window.location.search);
    
    let reqBody = {
        programmerId: urlParams.get('programmerId'),
        firstName: document.getElementById('programmer-fname-field').value,
        lastName: document.getElementById('programmer-lname-field').value,
        email: document.getElementById('programmer-email-field').value,
        mobile_number: document.getElementById('programmer-phone-field').value, 
        dateStarted: document.getElementById('project-date-started-field').value,
        accessLevel: document.getElementById('programmer-access-field').value
    };
    
    reqBody = JSON.stringify(reqBody);
    
    // Ajax request
    req.open("POST", PATH, true);
    req.setRequestHeader("Content-Type", "application/json");
    req.setRequestHeader("X-XSRF-TOKEN", Cookies.get('XSRF-TOKEN'));
    req.addEventListener("load", function redirectHome() {
        if (req.status >= 200 && req.status < 400) {
            // Clear the submit form and stop spinner
            setTimeout(() => { spinner.style.visibility = "hidden"; }, 1000);
            
            // Redirect to companies page
            window.location.href = "/bug_tracker/programmers";
        }
        else {
            setTimeout(() => { spinner.style.visibility = "hidden"; }, 1000);
            console.error('Database returned error: ' + req.status);
            alert("An error occurred connecting to the server.");
        }
    });
    
    req.send(reqBody);
};


// Onclick function so that pressing "cancel" returns the user to programmers list
document.getElementById("cancel").addEventListener("click", (event) => {
    event.preventDefault();
    window.location.href = "/bug_tracker/programmers";
});
