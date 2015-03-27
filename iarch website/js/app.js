$(function () {
$("#dvLoading").hide();

var client = new Dropbox.Client({ key: 'fapxgsf7glvwkb0' });
/*client.authDriver(new Dropbox.AuthDriver.Popup({
    receiverUrl: window.location.origin + '/oauth_receiver.html'
    //receiverUrl: 'http://localhost/lists-js-master/index.html/oauth_receiver.html'
}));*/

// Check to see if we're authenticated already.
client.authenticate({ interactive: false }, updateAuthenticationStatus);

// Authenticate when the user clicks the connect button.
$('#connect').click(function (e) {
    e.preventDefault();
    client.authenticate(updateAuthenticationStatus);
});

function escapeID(myid) {
    return "#" + myid.replace( /(:|\.|\[|\])/g, "\\$1" );
}

// Called when the authentication status changes.
function updateAuthenticationStatus(err, client) {
    // If the user is not authenticated, show the authentication modal
    if (!client.isAuthenticated()) {
        $('#login-modal').addClass('md-show');
        return;
    } else {
        $('#login-modal').removeClass('md-show');
    }

    client.getAccountInfo(function (err, info) {
            $('#name').text(info._json.display_name);
            $('#logout').text("Not " + info._json.display_name + "? Logout");      
    });

    // Once authenticated, find whether the user is on a team and
    // update UI accordingly.
    client.getAccountInfo(function (err, info) {
        if (info._json.team) {
            $('#teamName').text(info._json.team.name);
            $('#teamRole').show();
        }
    });

    var datastoreManager = client.getDatastoreManager();
    var datastore = null;
    var selectedDsid = null;
    var selectedRecId = null;
    var sortParameter = 'DATE';
    var date_ascending = true;
    var location_ascending = true;
    var artifact_ascending = true;
    var description_ascending = true;
    var gps_ascending = true;
    var tableName = "Picture_Data"
    var previousList = [];
    datastoreManager.datastoreListChanged.addListener(function (e) {
    var infos = e.getDatastoreInfos();


        // Update the list of projects on the left-hand side of the page
        $('#project-list ul').empty().append(
            _.chain(infos)
            // Sort by modified time
            .sortBy(function (info) {
                return info.getModifiedTime();
            })
            // Generate list items like this:
            // <li id="{datastore ID}">{title of datastore} <button class="enabled">X</button></li>
            .map(function (info) {
                var html = _.template('<li id="${dsid}"><div id="wrap">${title}</div></li>', {
                    dsid: info.getId(),
                    title: info.getTitle()
                });
                return $(html);
            })
            .value());

        // Highlighted the selected list
        if (selectedDsid) {
            $(escapeID(selectedDsid)).addClass('selected');
        }

        // Navigate to #{dsid} on click
        $('#project-list li').click(function (e) {
            e.preventDefault();
            window.location.hash = $(this).attr('id');
            return false;
        });

        // Handle delete project button click
        $("#project_delete").confirm({
                title:"Delete current project",
                text: "Are you sure you want to delete this entire project? This is permanent!",
                confirm: function(button) {
                    // If this is the list we're currently viewing
                    if (datastore !== null && datastore.getId() === selectedDsid) {
                        // Close the datastore and null it
                        datastore.close();
                        datastore = null;

                        // Select the first remaining list
                        var first = _.find($('#project-list li'), function (li) {
                            return $(li).attr('id') !== selectedDsid;
                        });
                        window.location.hash = $(first).attr('id') || '';
                    }
                    
                    // Delete the project directory with associated project images
                    client.remove(selectedDsid,
                        function (error, data) {
                            if (error) {
                                return console.log("ERROR: " + error); // Something went wrong.
                            }
                            
                            // otherwise directory is deleted                  
                    });    
                    
                    // Delete the datastore
                    datastoreManager.deleteDatastore(selectedDsid, function () { });

                    return false;
                    
                },
                cancel: function(button) {
                    // do nothing
                },
                confirmButton: "Yes I am",
                cancelButton: "No"
            });


        // Notify the user if the datastore they're viewing is removed
        var dsidList = _.map(e.getDatastoreInfos(), function (info) { return info.getId(); });
        var deleted = _.difference(previousList, dsidList);
        if (deleted.indexOf(selectedDsid) >= 0) {
            alert('The list you were viewing has been deleted, or your permissions have been revoked.');
            datastore.close();
            datastore = null;
            window.location.hash = $('#project-list li:first').attr('id') || '';
        }
        previousList = dsidList;

        // If we're viewing a list, update its UI based on the current
        // effective role.
        if (datastore) {
            updateUIBasedOnRole();
        }
    });

    function updateUIBasedOnRole() {
        if (datastore) {
            var editing = datastore.getEffectiveRole() !== 'viewer';

            // Delete buttons are only enabled if we're editing
            $('#items li button').toggleClass('enabled', editing);

            // New items can only be added if we're editing
            $('#newItem').toggle(editing);

            // Roles can only be changed if we're editing
            $('.role').prop('disabled', !editing);
        }
    }


    // Sort on different criteria (default is DATE)

    $('#sort_date').click(function (e) {
        e.preventDefault();
        sortParameter = 'DATE';
        if (date_ascending == true) // Prevoius click made date_ascending true, so now user wants to sort descending 
        {
            resetAscending(null);
            populateItemsDescending();
        }
        else // Sort on date ascending
        {
            resetAscending('date');
            populateItems();
        }      
    });

    $('#sort_location').click(function (e) {
        e.preventDefault();
        sortParameter = 'LOCATION';
        if (location_ascending == true) // Prevoius click made ascending true, so now user wants to sort descending 
        {
            resetAscending(null);
            populateItemsDescending();
        }
        else // Sort on date ascending
        {
            resetAscending('location');
            populateItems();
        }
    });

    $('#sort_artifact').click(function (e) {
        e.preventDefault();
        sortParameter = 'ARTIFACT_TYPE';
        if (artifact_ascending == true) // Prevoius click made ascending true, so now user wants to sort descending 
        {
            resetAscending(null);
            populateItemsDescending();
        }
        else // Sort on ascending
        {
            resetAscending('artifact');
            populateItems();
        }
    });

    $('#sort_description').click(function (e) {
        e.preventDefault();
        sortParameter = 'DESCRIPTION'
        if (description_ascending == true) // Prevoius click made ascending true, so now user wants to sort descending 
        {
            resetAscending(null);
            populateItemsDescending();
        }
        else // Sort on ascending
        {
            resetAscending('description');
            populateItems();
        }
    });

    $('#sort_gps').click(function (e) {
        e.preventDefault();
        sortParameter = 'GPS';
        if (gps_ascending == true) // Prevoius click made ascending true, so now user wants to sort descending 
        {
            resetAscending(null);
            populateItemsDescending();
        }
        else // Sort on ascending
        {
            resetAscending('gps');
            populateItems();
        }
    });

    // Helper function. Resets all ascending variables to false except for the appropriate variable clicked (if necessary)
    function resetAscending(field)
    {
        date_ascending = false;
        location_ascending = false;
        artifact_ascending = false;
        description_ascending = false;
        gps_ascending = false;
        if (field == 'date')
        {
            date_ascending = true;
        }
        if (field == 'location')
        {
            location_ascending = true;
        }
        if (field == 'artifact')
        {
            artifact_ascending = true;
        }
        if (field == 'description')
        {
            description_ascending = true;
        }
        if (field == 'gps')
        {
            gps_ascending = true;
        }
    }

    // Populate the right-hand side of the screen in ascending order (default)
    function populateItems() {
        $("#dvLoading").show();
        //$('#items h1').text(datastore.getTitle());
        var projectName = datastore.getId();
        var projectTitle = datastore.getTitle();
        $('#project-data h2').text(projectTitle);

        // Update the list, once on inital open and subsequently on
        // changes to the datastore.
        function updateList() {
            //var items = datastore.getTable('items').query();
            var items = datastore.getTable(tableName).query();
            var numItems = 0;
            
            // Rebuild the list of items
            $('#project-data tbody').empty().append(
                _.chain(items)
                // Sort by created date
                .sortBy(function (record) {
                    return record.get(sortParameter);
                }) //.reverse() for descending ordering
                // Convert to list items like this:
                // <li id="{record ID}"><button>X</button>{text}</li>
                .map(function (record) {
                    var fileName = record.get('LOCAL_FILENAME').split('/');
                    var filePath = projectName + '/' + fileName[fileName.length-1];
                    var thumbnail_url = client.thumbnailUrl(filePath, {size: "large"});
                    picture_url = "";
                    
                    /*finished = false;
                    console.log("finished before= " + finished);
                    
                    client.makeUrl(filePath, {
                        downloadHack: false,
                        long: true
                    }, function (error, data) {
                        if (error) {
                            return console.log("ERROR: " + error); // Something went wrong.
                        }
                        console.log("data.url= " + data.url);
                        picture_url = data.url.replace("www.dropbox.com","dl.dropboxusercontent.com");
                        console.log("picture_url inside= " + picture_url);
                        finished = true;                    
                    });
                                
                    console.log("finished after= " + finished);
                    console.log("Picture URL outside2= " + picture_url);*/
                    
                    numItems++;
                    var html = _.template('<tr id="${id}"><td id="num">${number}.</td><td id="thumb"><a href="${picture_url}" target="_blank"><img src="${thumbnail}" alt="Thumbnail"></a></td><td id="date">${date}</td><td id="location">${location}</td><td id="artifact">${artifact}</td><td id="description">${description}</td><td id="gps">${GPS}</td><td id="edit"><a href="#" id="record_edit"><span class="glyphicon glyphicon-pencil"></span></a><a href="#" id="record_delete"><span class="glyphicon glyphicon-trash"></span></a></td></tr>', {
                        id: record.getId(),
                        number: numItems,
                        pictureUrl: record.get('INTERNET_URL'),
                        thumbnail: thumbnail_url,
                        date: record.get('DATE'),
                        location: record.get('LOCATION'),
                        artifact: record.get('ARTIFACT_TYPE'),
                        description: record.get('DESCRIPTION'),
                        GPS: record.get('LATITUDE').toString() + " " + record.get('LONGITUDE').toString()
                    });

                    return $(html);

                }).value()
            );
            
            $("#dvLoading").hide();

            //updateUIBasedOnRole();

            // Reflect the latest ACLs in the sharing dialog
            //$('#public').val(datastore.getRole('public'));
            //$('#team').val(datastore.getRole('team'));
            //$('.role').prop('disabled', datastore.getEffectiveRole() === 'viewer');

            // Handle deleting a record
            $("#project-data tr a#record_delete").confirm({
                title:"Delete record",
                text: "Are you sure you want to delete this record?",
                confirm: function(button) {
                    var recordId = $(button).parents('tr').attr('id');
                    var filePath = getImageFilePath(recordId);

                    // Delete the datastore record
                    datastore.getTable(tableName).get(recordId).deleteRecord();

                    // Delete the image associated with that record
                    client.remove(filePath,
                        function (error, data) {
                            if (error) {
                                return console.log("ERROR: " + error); // Something went wrong.
                            }
                            
                            // otherwise file is deleted                  
                    });                    
                },
                cancel: function(button) {
                    // do nothing
                },
                confirmButton: "Yes I am",
                cancelButton: "No"
            });

            // Handle editing a record
            $("#project-data tr a#record_edit").click(function (e) {
                e.preventDefault();
                // Get the proper record details
                var recordId = $(this).parents('tr').attr('id');
                selectedRecId = recordId;
                var record = datastore.getTable(tableName).get(recordId);
                var location = record.get('LOCATION');
                var artifact = record.get('ARTIFACT_TYPE');
                var description = record.get('DESCRIPTION');

                // Show the form
                $('#form-modal').addClass('md-show');

                // Display current values in form
                $('#form_location').val(location);
                $('#form_artifact').val(artifact);
                $('#form_description').val(description);
            });
        }

        // Update on changes.
        datastore.recordsChanged.addListener(updateList);
        
        // Update UI with initial data.
        updateList();

    }

    // Populate the right-hand side of the screen in descending order
    function populateItemsDescending() {
        $("#dvLoading").show();
        //$('#items h1').text(datastore.getTitle());
        var projectName = datastore.getId();
        var projectTitle = datastore.getTitle();
        $('#project-data h2').text(projectTitle);

        // Update the list, once on inital open and subsequently on
        // changes to the datastore.
        function updateList() {
            //var items = datastore.getTable('items').query();
            var items = datastore.getTable(tableName).query();
            var numItems = 0;
            
            // Rebuild the list of items
            $('#project-data tbody').empty().append(
                _.chain(items)
                // Sort by created date
                .sortBy(function (record) {
                    return record.get(sortParameter);
                }).reverse() // for descending ordering
                // Convert to list items like this:
                // <li id="{record ID}"><button>X</button>{text}</li>
                .map(function (record) {
                    var fileName = record.get('LOCAL_FILENAME').split('/');
                    var filePath = projectName + '/' + fileName[fileName.length-1];
                    var thumbnail_url = client.thumbnailUrl(filePath, {size: "large"});
                    picture_url = "";
                    
                    /*finished = false;
                    console.log("finished before= " + finished);
                    
                    client.makeUrl(filePath, {
                        downloadHack: false,
                        long: true
                    }, function (error, data) {
                        if (error) {
                            return console.log("ERROR: " + error); // Something went wrong.
                        }
                        console.log("data.url= " + data.url);
                        picture_url = data.url.replace("www.dropbox.com","dl.dropboxusercontent.com");
                        console.log("picture_url inside= " + picture_url);
                        finished = true;                    
                    });
                                
                    console.log("finished after= " + finished);
                    console.log("Picture URL outside2= " + picture_url);*/
                    
                    numItems++;
                    var html = _.template('<tr id="${id}"><td id="num">${number}.</td><td id="thumb"><a href="${picture_url}" target="_blank"><img src="${thumbnail}" alt="Thumbnail"></a></td><td id="date">${date}</td><td id="location">${location}</td><td id="artifact">${artifact}</td><td id="description">${description}</td><td id="gps">${GPS}</td><td id="edit"><a href="#" id="record_edit"><span class="glyphicon glyphicon-pencil"></span></a><a href="#" id="record_delete"><span class="glyphicon glyphicon-trash"></span></a></td></tr>', {
                        id: record.getId(),
                        number: numItems,
                        pictureUrl: record.get('INTERNET_URL'),
                        thumbnail: thumbnail_url,
                        date: record.get('DATE'),
                        location: record.get('LOCATION'),
                        artifact: record.get('ARTIFACT_TYPE'),
                        description: record.get('DESCRIPTION'),
                        GPS: record.get('LATITUDE').toString() + " " + record.get('LONGITUDE').toString()
                    });

                    return $(html);

                }).value()
            );
            
            $("#dvLoading").hide();

            //updateUIBasedOnRole();

            // Reflect the latest ACLs in the sharing dialog
            //$('#public').val(datastore.getRole('public'));
            //$('#team').val(datastore.getRole('team'));
            //$('.role').prop('disabled', datastore.getEffectiveRole() === 'viewer');

            // Handle deleting a record
            $("#project-data tr a#record_delete").confirm({
                title:"Delete record",
                text: "Are you sure you want to delete this record?",
                confirm: function(button) {
                    var recordId = $(button).parents('tr').attr('id');
                    var filePath = getImageFilePath(recordId);

                    // Delete the datastore record
                    datastore.getTable(tableName).get(recordId).deleteRecord();

                    // Delete the image associated with that record
                    client.remove(filePath,
                        function (error, data) {
                            if (error) {
                                return console.log("ERROR: " + error); // Something went wrong.
                            }
                            
                            // otherwise file is deleted                  
                    });                    
                },
                cancel: function(button) {
                    // do nothing
                },
                confirmButton: "Yes I am",
                cancelButton: "No"
            });

            // Handle editing a record
            $("#project-data tr a#record_edit").click(function (e) {
                e.preventDefault();
                // Get the proper record details
                var recordId = $(this).parents('tr').attr('id');
                selectedRecId = recordId;
                var record = datastore.getTable(tableName).get(recordId);
                var location = record.get('LOCATION');
                var artifact = record.get('ARTIFACT_TYPE');
                var description = record.get('DESCRIPTION');

                // Show the form
                $('#form-modal').addClass('md-show');

                // Display current values in form
                $('#form_location').val(location);
                $('#form_artifact').val(artifact);
                $('#form_description').val(description);
            });
        }

        // Update on changes.
        datastore.recordsChanged.addListener(updateList);
        
        // Update UI with initial data.
        updateList();

    }

    // Update record when user has confirmed edits
    $( "#record_form_edit" ).submit(function (event) {
        event.preventDefault();
        // Grab the form values
        var location = $('#form_location').val();
        var artifact = $('#form_artifact').val();
        var description = $('#form_description').val();

        // Get the proper record
        var record = datastore.getTable(tableName).get(selectedRecId);

        // Update the record values
        record.set('LOCATION', location);
        record.set('ARTIFACT_TYPE', artifact);
        record.set('DESCRIPTION', description);

        // Hide edit form
        $('#form-modal').removeClass('md-show');
    });

    // Cancel submitting a record edit
    $("#form_cancel").click(function (e) {
        e.preventDefault();
        // Hide edit form
        $('#form-modal').removeClass('md-show');
    });

    // Get the file path of image relative to user's dropbox account
    function getImageFilePath(recordId) {
        var record = datastore.getTable(tableName).get(recordId);
        var projectName = record.get('PROJECT_NAME');
        var fileName = record.get('LOCAL_FILENAME').split('/');
        var filePath = projectName + '/' + fileName[fileName.length-1];
        return filePath;
    }

    // Handle the user selecting a list.
    function select(dsid) {
        // Ignore if this isn't a different list.
        if (selectedDsid === dsid) { return; }

        // Close sharing modal.
        $('#sharing-modal').removeClass('md-show');

        // Remember the selected DSID.
        selectedDsid = dsid;

        // If there's no selection, clear UI.
        if (!dsid) {
            $('#project-data ul').empty();
            $('#project-data h2').text('');
            $('#project-data').hide();
            $('#project-list li').removeClass('selected');
            if (datastore !== null) {
                datastore.close();
                datastore = null;
            }
            return;
        }

        // Update selection class.
        $('#project-list li').removeClass('selected');
        $(escapeID(dsid)).addClass('selected');

        // If this represents a change from the current datastore
        if (datastore === null || datastore.getId() !== dsid) {
            // Clear the right-hand side of the page.
            $('#project-data ul').empty();
            $('#project-data h2').text('');

            // Open the datastore
            datastoreManager.openDatastore(dsid, function (err, ds) {
                if (err) {
                    alert('Error opening list. Make sure you have the permission.');
                    return;
                }

                $('#project-data').show();

                // If the selection has already changed, ignore.
                if (ds.getId() !== selectedDsid) {
                    ds.close();
                    return;
                }

                // If there's an existing open datastore, close it.
                if (datastore !== null && datastore.getId() !== ds.getId()) {
                    datastore.close();
                    datastore = null;
                }

                // Store a pointer to the new open datastore.
                datastore = ds;

                // Update the UI with items from this datastore.
                populateItems();
            });
        }
    }

    $('#export').click(function (e) {
        e.preventDefault();
        alert("Coming soon!");
    });

    $('#share').click(function (e) {
        e.preventDefault();
        alert("Coming soon!");
    });

/*
    // Add a new list (datastore)
    $('#newList').submit(function (e) {
        e.preventDefault();
        var title = $('#listName').val();

        // Refuse empty titles
        if (title.length === 0) { return false; }

        // Clear the input
        $('#listName').val('');

        // Create the datastore
        datastoreManager.createDatastore(function (err, ds) {
            // Select it
            window.location.hash = ds.getId();

            // If there's an existing datastore, close it.
            if (datastore !== null && datastore.getId() !== ds.getId()) {
                datastore.close();
                datastore = null;
            }
            
            // Remember the new datastore we're looking at.
            datastore = ds;

            // Set the title
            datastore.setTitle(title);

            // Populate the right-hand side of the UI.
            populateItems();
        });

        return false;
    });
*/

    /*// Add a new item (record) to a list (datastore)
    $('#newItem').submit(function (e) {
        e.preventDefault();

        // Refuse empty strings
        if ($('#itemName').val().length === 0) { return false; }

        // Insert the record.
        datastore.getTable('items').insert({
            date: new Date(),
            text: $('#itemName').val()
        });

        // Clear the input
        $('#itemName').val('');

        return false;
    });*/

    // On hash changes, select the new list (datastore)
    $(window).hashchange(function (e) {
        e.preventDefault();
        select(window.location.hash.substring(1));
    });

    // Update the role when a new value is chosen
    $('select.role').change(function () {
        var principal = $(this).attr('id');
        var role = $(this).val();
        datastore.setRole(principal, role);
    });

    // Trigger the hash change once to pick up the DSID (if any) that
    // we started with.
    $(window).hashchange();
}

if (window.location.hash.substring(1) && /(iPad|iPhone|iPod)/g.test(navigator.userAgent)) {
    $('.md-modal').removeClass('md-show');
    $('#ios-modal').addClass('md-show');
    $('#ios').click(function (e) {
        e.preventDefault();
        window.location = 'lists://' + window.location.hash.substring(1);
    });
}

$('#logout').click(function () {
    client.signOut();
    window.location.reload();
});

$('.md-modal input').click(function () {
    $(this).select();
});

$('.sharing').click(function () {
    $('#url').val(window.location);
    $('#sharing-modal').addClass('md-show');
})

function closeModals() {
    $('#ios-modal, #sharing-modal').removeClass('md-show');

    // Reshow the auth modal if the user is not authenticated
    if (!client.isAuthenticated()) {
        $('#login-modal').addClass('md-show');
    }    
}

$('.md-overlay').click(function (e) {
    closeModals();
    e.preventDefault();
});

$(document).keydown(function (e) {
    if (e.which === 27) {
        closeModals();
        e.preventDefault();
        return false;
    }
});

});
