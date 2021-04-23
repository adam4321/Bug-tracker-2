/*************************************************************
**  Description: ADD COMPANY - Client-side JavaScript file
**************************************************************/

let recordForm = document.getElementById('recordForm');
let spinner = document.getElementById('spinner');
spinner.style.visibility = "hidden"; 


/* ADD COMPANY CLIENT SIDE ------------------------------------------------- */

// Function to submit the form data
recordForm.addEventListener('submit', (e) => {
    e.preventDefault();
    spinner.style.visibility = "visible"; 
    let req = new XMLHttpRequest();
    let path = '/bug_tracker/add_company/insertCompany';

    // String that holds the form data
    let reqBody = {
        companyName: recordForm.elements.companyName.value,
        dateJoined: recordForm.elements.dateJoined.value
    };

    reqBody = JSON.stringify(reqBody);

    // Ajax request
    req.open('POST', path, true);
    req.setRequestHeader('Content-Type', 'application/json');
    req.setRequestHeader("X-XSRF-TOKEN", Cookies.get('XSRF-TOKEN'));
    req.addEventListener('load', () => {
        if (req.status >= 200 && req.status < 400) {
            // Clear the submit form and stop spinner
            setTimeout(() => { spinner.style.visibility = "hidden"; }, 1000);
            
            // Redirect to companies page
            window.location.href = "/bug_tracker/companies";
        } else {
            console.error('Database return error');
        }
    });

    req.send(reqBody);
});
