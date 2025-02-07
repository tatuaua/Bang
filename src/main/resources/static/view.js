async function search() {
    let query = document.getElementById("query").value;
    let results = document.getElementById("results");

    results.innerHTML = "Searching...";

    if (!query || query.trim().length === 0) {
        results.innerHTML = "Please enter a query.";
        return;
    }

    if (hasNonAlphanumeric(query.split(" "))) {
        results.innerHTML = "Please enter a valid query. (Only alphanumeric characters are allowed.)";
        return;
    }

    try {
        let response = await fetch(`/search?q=${query.split(' ').join()}`, { method: 'POST' });

        if (!response.ok) {
            throw new Error(`Server error: ${response.status}`);
        }

        let data = await response.json();

        console.log("Response Data:", data);

        if (!Array.isArray(data) || data.length === 0) {
            results.innerHTML = "No results found.";
            return;
        }

        let ranking = rank(data);
        
        results.innerHTML = "";
        ranking.forEach((value, key) => {
            results.innerHTML += `${key} - ${value}\n`;
        });

    } catch (error) {
        console.error("Error fetching data:", error);
        results.innerHTML = "Something went wrong. Please try again later.";
    }
}

function rank(dataArray) {
    let ranking = new Map();

    dataArray.forEach((data) => {
        if (data.pageOccurrences && Array.isArray(data.pageOccurrences)) {
            data.pageOccurrences.forEach((pageOccurrence) => {
                ranking.set(pageOccurrence.page, (ranking.get(pageOccurrence.page) || 0) + pageOccurrence.occurrences);
            });
        }
    });

    return new Map([...ranking.entries()].sort((a, b) => b[1] - a[1]));
}

function hasNonAlphanumeric(arr) {
    return arr.some(str => /[^a-zA-Z0-9]/.test(str));
}

function handleDrop(event) {
    event.preventDefault();
    let file = event.dataTransfer.files[0];
    if (file) {
        uploadFile({ target: { files: [file] } });
    }
}

async function uploadFile(event) {
    let file = event.target.files[0];
    let status = document.getElementById("upload-status");

    if (!file) {
        status.innerHTML = "No file selected.";
        return;
    }

    let formData = new FormData();
    formData.append("file", file);

    try {
        let response = await fetch("/files/upload", {
            method: "POST",
            body: formData
        });

        if (!response.ok) {
            throw new Error(`Upload failed: ${response.status}`);
        }

        status.innerHTML = "File uploaded successfully!";
    } catch (error) {
        console.error("Upload error:", error);
        status.innerHTML = "Error uploading file.";
    }
}