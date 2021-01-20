//get latlng from url
let latlng = getUrlParameter('latlng');
let latlngArray = latlng.split(",");
//get data for given lat lng
let baseUrl = "http://127.0.0.1:5000"
let pathUrl = "/geo/ct/closest/5000"
let paramsUrl = "?latlng=" + latlng.toString()
$.get(baseUrl + pathUrl + paramsUrl)
    .done(function(data) {
        createPeople(data);
    });

// setup map
function initMap() {
    const map = new google.maps.Map(document.getElementById("map"), {
        mapId: "ab3ad70c84b8f9ff",
        center: { lat: parseFloat(latlngArray[0]), lng: parseFloat(latlngArray[1]) },
        zoom: 12,
    });
}


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
            ${person.party_code == 'D' ? `democrat` : ''} ${person.party_code == 'R' ? `republican` : ''}
             voter-id="${person.voter_id}" miles="${person.miles}"  
             id="person-${index + 1}">
                    <span class="name">${person.first_name} ${person.last_name.charAt(0)}.</span>
                    <span class="dob">${person.dob.substring(person.dob.length - 2)}'</span>
        
            </div>
        `;
        $('.people-container').append(markup);
    });
    $("#cover-spin").hide();
    // <div class="person-container republican" voter-id="12345">
    //     <span class="name">Will S.</span>
    //     <span class="dob">00'</span>
    // </div>
}