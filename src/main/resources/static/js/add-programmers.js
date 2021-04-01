/*************************************************************
**  Description: ADD PROGRAMMER - Client-side JavaScript file
**************************************************************/

let recordForm = document.getElementById('recordForm');
let spinner = document.getElementById('spinner');
spinner.style.visibility = "hidden"; 

// Function to submit the form data
recordForm.addEventListener('submit', (e) => {
    e.preventDefault();
    spinner.style.visibility = "visible"; 
    let req = new XMLHttpRequest();
    let path = '/bug_tracker/programmers/insertProgrammer';

    // String that holds the form data
    let reqBody = {
        firstName: recordForm.elements.programmerFName.value,
        lastName: recordForm.elements.programmerLName.value,
        email: recordForm.elements.userEmail.value,
        dateStarted: recordForm.elements.startedDate.value,
        accessLevel: recordForm.elements.programmerAccess.value
    };

    reqBody = JSON.stringify(reqBody);

    // Ajax request
    req.open('POST', path, true);
    req.setRequestHeader('Content-Type', 'application/json');
    req.addEventListener('load', () => {
        if (req.status >= 200 && req.status < 400) {
            let response = JSON.parse(req.responseText);
            let id = response.projects;

            // Table of database records for the added companies
            let tbl = document.getElementById('recordTable');
            let newRow = tbl.insertRow(-1);

            // First Name element
            let nameCell = document.createElement('td');
            nameCell.textContent = `${recordForm.elements.programmerFName.value} ${recordForm.elements.programmerLName.value}`;
            nameCell.className = 'mdl-data-table__cell--non-numeric'; 
            newRow.appendChild(nameCell);

            // Email element
            let emailCell = document.createElement('td');
            emailCell.textContent = recordForm.elements.userEmail.value;
            emailCell.className = 'mdl-data-table__cell--non-numeric';
            newRow.appendChild(emailCell);

            // Date Started element
            let startedCell = document.createElement('td');
            startedCell.textContent = recordForm.elements.startedDate.value;
            newRow.appendChild(startedCell);

            // Access level element
            let levelCell = document.createElement('td');
            levelCell.textContent = recordForm.elements.programmerAccess.value;
            newRow.appendChild(levelCell);

            // Clear the submit form
            document.getElementById('recordForm').reset();
            setTimeout(() => { spinner.style.visibility = "hidden"; }, 1000);

        } else {
            console.error('Database return error');
        }
    });
    req.send(reqBody);
});
