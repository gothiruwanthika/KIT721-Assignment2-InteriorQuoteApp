# Interior Quote App

## Student
- Name: Gothami Jayasinghe
- Unit: KIT721 Mobile Application Development

## Assignment
Assignment 2 - Android Application

## Current Progress
### Stage 1
- Created Android project in Kotlin
- Enabled View Binding
- Connected Firebase
- Created Firestore database
- Confirmed Firestore write test successfully
- Pushed project to GitHub

### Stage 2
- Created House data class
- Replaced starter screen with House List empty-state screen
- Added Floating Action Button for creating a new house
- Confirmed Add button click interaction with Toast message


### Stage 3
- Created AddEditHouseActivity
- Built New House form screen
- Connected New House button to open the form
- Added validation for required fields
- Confirmed valid form submission flow
- Improved Add House form to better match prototype
- Added inline validation messages
- Added 10-digit contact number validation
- Added back navigation on Add House screen

### Stage 4
- Saved house data to Firebase Firestore
- Loaded saved houses from Firestore in MainActivity
- Implemented RecyclerView for house list display
- Created HouseAdapter and house_item layout
- Switched between empty state and house list state depending on available data

### Stage 5
- Created HouseDetailsActivity
- Passed selected house data from MainActivity
- Displayed customer name and address on House Details screen
- Added back navigation, Edit button placeholder, Add Room button, and Generate Quote button

### Stage 6
- Created AddEditRoomActivity
- Built Add Room form based on prototype
- Added room validation for name and labour cost
- Passed houseId from HouseDetailsActivity to AddEditRoomActivity
- Saved rooms under each house in Firestore subcollections
- Loaded and displayed rooms in HouseDetailsActivity using RecyclerView
- Added room include/exclude checkbox on House Details screen
- Saved includedInQuote state to Firestore
- Matched updated prototype for room selection before quote generation


### Stage 7
- Created RoomDetailsActivity
- Passed selected room data from HouseDetailsActivity
- Displayed room name and labour cost on Room Details screen
- Added placeholders for room photo, windows, and floor spaces
- Added Edit, Add Window, Add Floor Space, and Delete buttons

### Stage 8
- Created AddEditWindowActivity
- Built Add Window form with validation for name, width, and height
- Added required selected product field to match prototype
- Created SelectProductActivity for basic product selection
- Returned selected product from SelectProductActivity to AddEditWindowActivity
- Saved windows under the correct room in Firestore
- Displayed saved windows in RoomDetailsActivity using RecyclerView


### Stage 9
- Created AddEditFloorSpaceActivity
- Built Add Floor Space form based on prototype
- Added validation for floor space name, width, and length
- Reused SelectProductActivity for floor product selection
- Saved floor spaces under the correct room in Firestore
- Displayed saved floor spaces in RoomDetailsActivity using RecyclerView
## Test Device / Emulator
- Medium Phone API 35 Emulator

## Activities
### MainActivity
Used for initial project setup and Firebase/Firestore connection testing.

## References
- Firebase Documentation
- Android Studio Documentation
- KIT721 tutorials and assignment documents

## GenAI Use
This project was developed with planning, debugging, and explanatory support from ChatGPT. All implementation decisions were reviewed and tested by the student.