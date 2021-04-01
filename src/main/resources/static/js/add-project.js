/*************************************************************
**  Description: ADD PROJECT - Client-side JavaScript file
**************************************************************/

let recordForm = document.getElementById('recordForm');
let spinner = document.getElementById('spinner');
spinner.style.visibility = "hidden"; 

// Function to submit the form data
recordForm.addEventListener('submit', (e) => {
    e.preventDefault();
    spinner.style.visibility = "visible";
    let req = new XMLHttpRequest();
    let path = '/bug_tracker/projects/add_project/insertProject';

    // String that holds the form data
    let reqBody = {
        projectName: recordForm.elements.projectName.value,
        companyName: recordForm.elements.projectCompany.value,
        dateStarted: recordForm.elements.startedDate.value,
        lastUpdated: recordForm.elements.updatedDate.value,
        inMaintenance: recordForm.elements.maintenance.value
    };

    reqBody = JSON.stringify(reqBody);

    // Ajax request
    req.open('POST', path, true);
    req.setRequestHeader('Content-Type', 'application/json');
    req.addEventListener('load', () => {
        if (req.status >= 200 && req.status < 400) {
            // Clear the submit form and turn off the spinner
            document.getElementById('recordForm').reset();
            setTimeout(() => { spinner.style.visibility = "hidden"; }, 1000);

            // Redirect to companies page
            window.location.href = "/bug_tracker/projects";
        } else {
            console.error('Database return error');
        }
    });

    req.send(reqBody);
});
