var http = require('http'),
    fs = require('fs'),
    url = require('url'),
    pdfUtil = require('pdf-to-raw');

var pdf_path = "/home/jamie/Documents/in.pdf";
var persons = [];
var debug = false;

function findDataPositions(rawPersonArr) {
    
    dataPos = {
        nameData : {pos : -1, endPos: -1},
        dobData : {pos : -1, endPos: -1},
        facebookData : {pos : -1, endPos: -1},
        knownAssData : {pos : -1, endPos: -1},
        assCrimesData : {pos : -1, endPos: -1},
        addInfoData : {pos : -1, endPos: -1}
    };

    posArr = [];

    for(j = 0; j < rawPersonArr.length; j++) {
        if(rawPersonArr[j].indexOf("Name: ") >= 0) {
            dataPos.nameData.pos = j;
            posArr.push({var:"nameData", pos: j});
        } else if(rawPersonArr[j].indexOf("Date of Birth:") >= 0) {
            dataPos.dobData.pos = j;
            posArr.push({var:"dobData", pos: j});
        } else if(rawPersonArr[j].indexOf("Facebook page:") >= 0 ||
                        rawPersonArr[j].indexOf("Facebook page\(s\):") >= 0 ||
                        rawPersonArr[j].indexOf("Facebook pages:") >= 0) {
            dataPos.facebookData.pos = j;
            posArr.push({var:"facebookData", pos: j});
        } else if(rawPersonArr[j].indexOf("Known associates:") >= 0) {
            dataPos.knownAssData.pos = j;
            posArr.push({var:"knownAssData", pos: j});
        } else if(rawPersonArr[j].indexOf("Associated crimes:") >= 0) {
            dataPos.assCrimesData.pos = j;
            posArr.push({var:"assCrimesData", pos: j});
        } else if(rawPersonArr[j].indexOf("Additional information:") >= 0 ||
                        rawPersonArr[j].indexOf("Additional Information:") >= 0) {
            dataPos.addInfoData.pos = j;
            posArr.push({var:"addInfoData", pos: j});
        }
    }

    if(debug) {
        if(dataPos.nameData.pos < 0)
            console.log("No Name Data");
        
        if(dataPos.dobData.pos < 0)
            console.log("No DOB data");
        
        if(dataPos.facebookData.pos < 0)
            console.log("No Facebook Data");
        
        if(dataPos.knownAssData.pos < 0)
            console.log("No Known Associates Data");

        if(dataPos.assCrimesData.pos < 0)
            console.log("No associated crime data");
        
        if(dataPos.addInfoData.pos < 0)
            console.log("No additional information data");
    }

    posArr.sort(function(a,b) {
        if(a.pos < b.pos)
            return -1;
        if(a.pos > b.pos)
            return 1;
        return 0;
    });

    for(j = 0; j < posArr.length-1; j++) {
        switch(posArr[j].var) {
            case "nameData":
                dataPos.nameData.endPos = posArr[j+1].pos-1;
                break;
            case "dobData":
                dataPos.dobData.endPos = posArr[j+1].pos-1;
                break;
            case "facebookData":
                dataPos.facebookData.endPos = posArr[j+1].pos-1;
                break;
            case "knownAssData":
                dataPos.knownAssData.endPos = posArr[j+1].pos-1;
                break;
            case "assCrimesData":
                dataPos.assCrimesData.endPos = posArr[j+1].pos-1;
                break;
            case "addInfoData":
                dataPos.addInfoData.endPos = posArr[j+1].pos-1;
                break;
        }
    }
    switch(posArr[posArr.length-1].var) {
        case "nameData":
            dataPos.nameData.endPos = rawPersonArr.length-1;
            break;
        case "dobData":
            dataPos.dobData.endPos = rawPersonArr.length-1;
            break;
        case "facebookData":
            dataPos.facebookData.endPos = rawPersonArr.length-1;
            break;
        case "knownAssData":
            dataPos.knownAssData.endPos = rawPersonArr.length-1;
            break;
        case "assCrimesData":
            dataPos.assCrimesData.endPos = rawPersonArr.length-1;
            break;
        case "addInfoData":
            dataPos.addInfoData.endPos = rawPersonArr.length-1;
            break;
    }

    return dataPos
}

function parseAssociates(associates) {
    relation = false;
    position = 0;
    associateStrings = [];
    associateArray = [];

    while(associates.length > 0) {
        if(associates.length == position) {
            associateStrings.push(associates);
            break;
        } else if(associates.substr(position,1) == ',' && !relation) {
            associateStrings.push(associates.substr(0, position).trim());
            associates = associates.substr(position+1, associates.length);
            position = 0;
        } else if (associates.substr(position,1) == '(') {
            relation = true;
            position++;
        } else if(associates.substr(position,1) == ')') {
            relation = false;
            position++;
        } else {
            position++;
        }
    }
    
    for(n = 0; n < associateStrings.length; n++) {
        if(associateStrings[n].length > 0) {
            if(associateStrings[n].split("(").length > 1) {
                associateArray.push(
                    {
                        name: associateStrings[n].split("(")[0],
                        relation: associateStrings[n].split("(")[1].substr(0, associateStrings[n].split("(")[1].length-1)
                    }
                );
            } else {
                associateArray.push(
                    {
                        name: associateStrings[n],
                        relation: null
                    });
            }
        }
    }
    return associateArray;
}

pdfUtil.pdfToRaw(pdf_path, function(err, data) {
    if (err) throw(err);
    
    //Remove page and update information from the PDF
    pdfData = data.split("\n");
    data = "";
    for(i = 0; i < pdfData.length; i++) {
        if(pdfData[i].indexOf("Page ") < 0)
            data = data + pdfData[i] + "\n";
    }

    // Split the data in to individuals
    pdfData = data.split("Name: ");

    for(i = 1; i < pdfData.length; i++) {
        
        personData = {
            name:"",
            aka:"",
            dob:"",
            facebook:[],
            knownAssociates:[],
            associatedCrimes:[],
            additionalInformation:""
        }

        rawPerson = "Name: " + pdfData[i];
        rawPersonArr = rawPerson.split("\n");

        nameData = "";
        dobData = "";
        facebookData = [];
        knownAssData = "";
        assCrimesData = "";
        addInfoData = "";

        dataPositons = findDataPositions(rawPersonArr);

        //Look for Name data
        for(x = dataPositons.nameData.pos; x <= dataPositons.nameData.endPos; x++) {
            nameData = nameData + rawPersonArr[x];
        }
        if(nameData.length > 0) {
            if(nameData.split(": ").length > 1) {
                nameData = nameData.split(": ")[1];
                if(nameData.split("(").length > 1) {
                    personData.name = nameData.split("(")[0];
                    personData.aka = nameData.split("(")[1].substr(0, nameData.split("(")[1].length-1);
                } else {
                    personData.name = nameData;
                }
            } else {
                console.log("Error processing name data");
                personData.name = nameData;
            }
        }

        //Look for DOB data
        for(x = dataPositons.dobData.pos; x <= dataPositons.dobData.endPos; x++) {
            dobData = dobData + rawPersonArr[x];
        }
        if(dobData.length > 0) {
            personData.dob = dobData.split(": ")[1];
        }

        //Look for Facebook Data
        for(x = dataPositons.facebookData.pos; x <= dataPositons.facebookData.endPos; x++) {
            facebookData.push(rawPersonArr[x]);
        }
        if(facebookData.length > 0) {
            link0 = facebookData[0].split(": ")[1];
            if(link0.split("(").length > 1) {
                personData.facebook.push(
                    {
                        link: link0.split("(")[0],
                        status: link0.split("(")[1].substr(0, link0.split("(")[1].length-1)
                    }
                );
            } else {
                personData.facebook.push({link: link0, status: null});
            }
            for(x = 1; x < facebookData.length; x++) {
                if(facebookData[x].split("(").length > 1) {
                    personData.facebook.push({link: facebookData[x].split("(")[0], status: facebookData[x].split("(")[1].substr(0, facebookData[x].split("(")[1].length-1)});
                } else {
                    personData.facebook.push({link: facebookData[x], status: null});
                }
            }
        }

        //Look for known associates Data
        for(x = dataPositons.knownAssData.pos; x <= dataPositons.knownAssData.endPos; x++) {
            knownAssData = knownAssData + rawPersonArr[x] + " ";
        }
        if(knownAssData.length > 0) {
            if(knownAssData.split(": ").length > 1) {
                
                knownAssData = knownAssData.split(": ")[1];
                personData.knowAssociates = parseAssociates(knownAssData);

            }
        }

        // Look for Associated crimes data
        for(x = dataPositons.assCrimesData.pos; x <= dataPositons.assCrimesData.endPos; x++) {
            assCrimesData = assCrimesData + rawPersonArr[x]+"\n";
        }
        if(assCrimesData.length > 0) {
            personData.associatedCrimes = assCrimesData;
        }

        // Look for Additional info
        for(x = dataPositons.addInfoData.pos; x <= dataPositons.addInfoData.endPos; x++) {
            addInfoData = addInfoData + rawPersonArr[x];
        }
        if(addInfoData.length > 0) {
            personData.additionalInformation = addInfoData;
        }

        var request = require('request');
        request.post('http://localhost:8080/import-person', {json: true, body: personData}, function(err,res,body) {
            if(!err && res.statusCode === 200) {
                console.log("Imported " + body.name + " with id : " + body.importId);
            } else {
                console.log("error: " + err);
                console.log("result status:" + res.statusCode);
            }
        });
        persons.push(personData);
    }
});