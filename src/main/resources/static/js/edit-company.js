/***************************************************************
**  Description: UPDATE COMPANY - Client-side JavaScript file
***************************************************************/

let recordForm = document.getElementById('recordForm');
let spinner = document.getElementById('spinner');
spinner.style.visibility = "hidden"; 


/* UPDATE COMPANY CLIENT SIDE ---------------------------------------------- */

// Function to submit the form data
recordForm.addEventListener('submit', (e) => {
    e.preventDefault();
    spinner.style.visibility = "visible"; 
    let req = new XMLHttpRequest();
    let path = '/bug_tracker/edit_company/updateCompany';

    const urlParams = new URLSearchParams(window.location.search);

    // String that holds the form data
    let reqBody = {
        companyId: urlParams.get('companyId'),
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
        } 
        else {
            setTimeout(() => { spinner.style.visibility = "hidden"; }, 1000);
            console.error('Database returned error: ' + req.status);
        }
    });

    req.send(reqBody);
});
