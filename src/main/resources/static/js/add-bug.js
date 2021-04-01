/*************************************************************
**  Description: ADD-BUG client-side JavaScript file
**************************************************************/


/* INSERT BUG VERIFY PROGRAMMER CHECKED -------------------------------------*/

// Function to verify that at least 1 programmer is checked
function checkBoxChecked() {
    const form = document.querySelector('#recordForm');
    const checkboxes = form.querySelectorAll('input[type=checkbox]');
    const checkboxLength = checkboxes.length;
    const firstCheckbox = checkboxLength > 0 ? checkboxes[0] : null;

    function init() {
        if (firstCheckbox) {
            for (let i = 0; i < checkboxLength; i++) {
                checkboxes[i].addEventListener('change', checkValidity);
            }

            checkValidity();
        }
    }

    function isChecked() {
        for (let i = 0; i < checkboxLength; i++) {
            if (checkboxes[i].checked) return true;
        }

        return false;
    }

    function checkValidity() {
        const errorMessage = !isChecked() ? 'At least one checkbox must be selected.' : '';
        firstCheckbox.setCustomValidity(errorMessage);
    }

    init();
};


/* INSERT BUG CLIENT SIDE -------------------------------------------------- */

// Function to submit the bug's form data
let recordForm = document.getElementById('recordForm');
let spinner = document.getElementById('spinner');
spinner.style.visibility = "hidden";

recordForm.addEventListener('submit', (e) => {
    e.preventDefault();
    spinner.style.visibility = "visible"; 
    let req = new XMLHttpRequest();
    let path = '/bug_tracker/add_bug/insertBug';

    // Iterate over the checked programmers to create http query sub-string
    let programmerArr = [];
    for (let i = 0; i < recordForm.elements.length; i++) {
        try {
            if (recordForm.elements.programmerId[i].checked) {
                programmerArr.push(recordForm.elements.programmerId[i].value);
            }
        } catch(e) {
            continue;
        }
    }

    // Gather the selected programmer's names for rendering to the new cell
    let programmerList = [];
    let programmerCount = 0;
    for (let i = 0; i < recordForm.elements.length; i++) {
        try {
            if (recordForm.elements.programmerId[i].checked) {
                programmerCount++;
                programmerList.push(recordForm.elements.programmerId[i].nextElementSibling.innerHTML);
            }
        } catch(e) {
            continue;
        }
    }

    // Fill the project, if it has a value
    let project;
    if (recordForm.elements.bugProject.value == 'null') {
            recordForm.elements.bugProject.value = '';
        }
    if (recordForm.elements.bugProject.value) {
        project = recordForm.elements.bugProject.value;
    }

    // String that holds the form data
    let reqBody = {
        bugSummary: recordForm.elements.bugSummary.value,
        bugDescription: recordForm.elements.bugDescription.value,
        bugProject: project,
        programmerArr: programmerArr,
        bugStartDate: recordForm.elements.bugStartDate.value,
        bugPriority: recordForm.elements.bugPriority.value,
        bugFixed: recordForm.elements.bugFixed.value,
        bugResolution: recordForm.elements.bugResolution.value
    };

    reqBody = JSON.stringify(reqBody);

    // Ajax request
    req.open('POST', path, true);
    req.setRequestHeader('Content-Type', 'application/json');
    req.addEventListener('load', () => {
        if (req.status >= 200 && req.status < 400) {
            // Clear the submit form and reset the spinner
            document.getElementById('recordForm').reset();
            setTimeout(() => { spinner.style.visibility = "hidden"; }, 1000);

            // Redirect to all bugs
            window.location.href = '/bug_tracker/all_bugs';
        } else {
            console.error('Database return error');
        }
    });

    req.send(reqBody);
});
