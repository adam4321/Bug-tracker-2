/*************************************************************
**  Description: PROJECTS - Client-side JavaScript file
**************************************************************/

/* DELETE PROJECT CLIENT SIDE -------------------------------------------------- */

// Function call to delete a row from projects
function deleteProject(tbl, curRow, projectId) {
    let delete_confirm = confirm("Are you sure?");

    if (delete_confirm) {
        let table = document.getElementById(tbl);
        let rowCount = table.rows.length;
        let req = new XMLHttpRequest();
        let path = "/bug_tracker/projects/deleteProject";
    
        reqBody = JSON.stringify({projectId: projectId});
    
        req.open("POST", path, true);
        req.setRequestHeader("Content-Type", "application/json");
        req.addEventListener("load", () => {
            if (req.status >= 200 && req.status < 400) {
                for (let i = 0; i < rowCount; i++) {
                    let row = table.rows[i];
            
                    if (row == curRow.parentNode.parentNode) {
                        table.deleteRow(i);
                    }
                }
            } 
            else {
                console.error("Delete request error");
            }
        });
    
        req.send(reqBody);
    }
}
