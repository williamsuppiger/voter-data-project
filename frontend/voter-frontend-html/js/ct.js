//constants
const politicalGradient = {
    red: { red: 187, green: 1, blue: 3 },
    white: { red: 190, green: 190, blue: 190 },
    blue: { red: 18, green: 7, blue: 163 }
}
const maxCount = 1000;

//get latlng from url
const latlng = getUrlParameter('latlng');
const latlngArray = latlng.split(",");

//get data for given lat lng
let baseUrl = "http://127.0.0.1:5000";
let pathUrl = "/geo/ct/closest/" + maxCount;
let paramsUrl = "?latlng=" + latlng.toString();
let data;
let dataReady = false;
let polyRadius;
let map;
$.get(baseUrl + pathUrl + paramsUrl)
    .done(function(fetchedData) {
        data = fetchedData;
        dataReady = true;
        createPeople(fetchedData);
    });

// setup map
function initMap() {
    let coords = { lat: parseFloat(latlngArray[0]), lng: parseFloat(latlngArray[1]) };
    map = new google.maps.Map(document.getElementById("map"), {
        mapId: "ab3ad70c84b8f9ff",
        center: coords,
        zoom: 19,
    });

    polyRadius = new google.maps.Circle({
        strokeColor: 'lightgrey',
        strokeOpacity: 0.8,
        strokeWeight: 0,
        fillColor: 'lightgrey',
        fillOpacity: 0.7,
        map,
        center: coords,
        radius: 5,
    });
}

//counts number of people scrolled past on people container element
function countScrollPast() {
    //calculate person height
    const personElementHeight = $(window).height() * 0.12;
    let scrollDepth = $('.people-container').scrollTop();
    var count = scrollDepth / personElementHeight;
    // return either current count or last record
    return Math.min(Math.floor(count), maxCount - 1)
}

let currentScrolledPast = 0;

// on scroll of container
$('.people-container').scroll(function() {
    clearTimeout($.data(this, 'scrollTimer'));
    $.data(this, 'scrollTimer', setTimeout(function() {
        if (currentScrolledPast != countScrollPast()) {
            //update new spot
            currentScrolledPast = countScrollPast();
            //count reps vs dems
            let reps = dems = other = totalAge = 0;
            for (let i = 0; i < currentScrolledPast; i++) {
                // if record exists
                if (data.records[i]) {
                    // add party to count
                    let party = data.records[i].party_code;
                    if (party == 'R') {
                        reps++;
                    } else if (party == 'D') {
                        dems++;
                    } else {
                        other++;
                    }
                    //calc age
                    totalAge += calculateAge(textToDate(data.records[i].dob));
                }
            }
            let averageAge = totalAge / currentScrolledPast;
            //get new color of map
            let color = colorGradient((dems / (reps + dems)), politicalGradient.red,
                politicalGradient.white, politicalGradient.blue);
            //get new radius of circle
            let radius = Math.max(5, getMeters(data.records[currentScrolledPast].miles));
            //update circle
            polyRadius.setOptions({ fillColor: color, radius: radius });
            // set data
            $('#currentCount').text(currentScrolledPast);
            $('#currentRadius').text(data.records[currentScrolledPast].miles.toFixed(2));
            $('#averageAge').text(averageAge.toFixed(1));
            $('#repCount').text(reps);
            $('#demCount').text(dems);
            $('#independentCount').text(other);
        }
    }, 50));
});

function getUrlParameter(sParam) {
    var sPageURL = window.location.search.substring(1),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return sParameterName[1] === undefined ? true : decodeURIComponent(sParameterName[1]);
        }
    }
};

function createPeople(data) {
    jQuery.each(data.records, function(index, person) {
                let markup = `
            <div class="person-container 
            ${person.party_code == 'D' ? `democrat` : ''} ${person.party_code == 'R' ? `republican` : ''}"
             voter-id="${person.voter_id}" id="person-${index}" onclick="showPersonModal(this)">
                    <span class="name">${person.first_name} ${person.last_name.charAt(0)}.</span>
                    <span class="dob">${calculateAge(textToDate(person.dob))}</span>
        
            </div>
        `;
        $('.people-container').append(markup);
    });
    $("#cover-spin").hide();
    $("#instructions-close").show();
}

// credit: https://gist.github.com/gskema/2f56dc2e087894ffc756c11e6de1b5ed 
function colorGradient(fadeFraction, rgbColor1, rgbColor2, rgbColor3) {
    var color1 = rgbColor1;
    var color2 = rgbColor2;
    var fade = fadeFraction;

    // Do we have 3 colors for the gradient? Need to adjust the params.
    if (rgbColor3) {
        fade = fade * 2;

        // Find which interval to use and adjust the fade percentage
        if (fade >= 1) {
            fade -= 1;
            color1 = rgbColor2;
            color2 = rgbColor3;
        }
    }

    var diffRed = color2.red - color1.red;
    var diffGreen = color2.green - color1.green;
    var diffBlue = color2.blue - color1.blue;

    var gradient = {
        red: parseInt(Math.floor(color1.red + (diffRed * fade)), 10),
        green: parseInt(Math.floor(color1.green + (diffGreen * fade)), 10),
        blue: parseInt(Math.floor(color1.blue + (diffBlue * fade)), 10),
    };

    return 'rgb(' + gradient.red + ',' + gradient.green + ',' + gradient.blue + ')';
}

// show info person modal on load
$( document ).ready(function() {
    $('.popup_person_modal').show(); 
});

// popup modal when click on person for more info
function showPersonModal(e) {
    $('.info_modal_text').hide();
    let voterId = e.getAttribute("voter-id");
    let relativeUrl = "/voter/ct/" + voterId + "/info"
    $.get(baseUrl + relativeUrl)
        .done(function (fetchedData) {
            console.log(fetchedData);
            let p = fetchedData.voter
            //set data
            $('#p_full_name').text(p.first_name + " " + p.middle_name + " " + p.last_name);
            $('#p_gender').text(p.gender);
            $('#p_dob').text(p.dob);
            $('#p_address').text(p.address_number + " " + p.street_name + ", "
                + p.town_name + ", " + p.state + " " + p.zip5);
            $('#p_phone_number').text(formatPhoneNumber(p.phone_number));
            $('#p_party_code').text(p.party_code);
            $('#p_registration_date').text(p.registration_date);
            $('#p_voter_id').text(p.voter_id);
            $('#p_voting_district').text(p.voting_district);
            $('#p_voting_precinct').text(p.voting_precinct);
            let electionHistory = p.election_history.split(",");
            let filteredElection = electionHistory.filter(function (el) {
                return (el != null && el.trim().length > 2);
            });
            $('#p_election_history').text(filteredElection.join(", "));
            $('.popup_person_modal').show();
            $('.person_modal_text').show();
        });
}

function closeModal() {
    $('.popup_person_modal').hide();
}

// credit: https://stackoverflow.com/a/64947598/7873106
const waitUntil = (condition) => {
    return new Promise((resolve) => {
        let interval = setInterval(() => {
            if (!condition()) {
                return
            }

            clearInterval(interval)
            resolve()
        }, 100)
    })
}

function componentToHex(c) {
    let hex = c.toString(16);
    return hex.length == 1 ? "0" + hex : hex;
}

function rgbToHex(r, g, b) {
    return "#" + componentToHex(r) + componentToHex(g) + componentToHex(b);
}

function getMeters(i) {
    return i * 1609.344;
}

function textToDate(text) {
    var date = text.split("/");
    return new Date(date[2], date[0] - 1, date[1]);
}

function calculateAge(birthday) { // birthday is a date
    if (birthday == "Invalid Date") {
        return "";
    }
    var ageDifMs = Date.now() - birthday.getTime();
    var ageDate = new Date(ageDifMs); // miliseconds from epoch
    return Math.abs(ageDate.getUTCFullYear() - 1970);
}

// credit: https://learnersbucket.com/examples/javascript/how-to-format-phone-number-in-javascript/
let formatPhoneNumber = (str) => {
    //Filter only numbers from the input
    let cleaned = ('' + str).replace(/\D/g, '');

    //Check if the input is of correct length
    let match = cleaned.match(/^(\d{3})(\d{3})(\d{4})$/);

    if (match) {
        return '(' + match[1] + ') ' + match[2] + '-' + match[3]
    };

    return null
};