/**********************************************************************************
**  Description:   Update account page client side JavaScript
**********************************************************************************/

/* RESET DATABASE CLIENT SIDE ---------------------------------------------- */

// Function to drop and repopulate all database tables
let resetBtn = document.getElementById("reset-table");
resetBtn.addEventListener('click', resetTable);
let spinner2 = document.getElementById('spinner2');
spinner2.style.visibility = "hidden";

function resetTable() {
    let path = "/bug_tracker/admin/resetTable";
    let req = new XMLHttpRequest();

    // Prompt the user for a confirmation before resetting the db
    let confirmVal = confirm(`This button wipes your account, RESETS the database, and repopulates it with sample data!\n\nPress cancel to abort.`);

    if (confirmVal) {
        // Display the spinner
        spinner2.style.visibility = "visible";

        // Make the ajax request
        req.open("POST", path, true);   
        req.setRequestHeader("Content-Type", "application/json");
        req.setRequestHeader("X-XSRF-TOKEN", Cookies.get('XSRF-TOKEN'));
        req.send(); 

        req.addEventListener("load", () => {
            if (req.status >= 200 && req.status < 400) {
                // Log the user out, because they no longer exist in the system (declared in main.js)
                logout();
            } 
            else {
                console.error("Reset table request error.");
            }
        })
    }
}
