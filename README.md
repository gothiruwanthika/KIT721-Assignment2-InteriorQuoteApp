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

### Stage 10
- Created QuoteActivity
- Connected Generate Quote button from HouseDetailsActivity
- Loaded only included rooms into the quote
- Displayed windows, floor spaces, labour cost, and room totals
- Calculated final total quote
- Added working share quote functionality

### Stage 11
- Implemented room deletion with confirmation dialog
- Updated room deletion to remove associated windows and floor spaces before deleting the room
- Added delete support for individual windows from Room Details
- Added delete support for individual floor spaces from Room Details
- Implemented Edit Room using AddEditRoomActivity in edit mode
- Implemented Edit House using AddEditHouseActivity in edit mode

### Stage 12
- Implemented room photo selection from gallery
- Saved room photos in Firestore using base64 image data
- Loaded and displayed saved room photos in RoomDetailsActivity
- Kept placeholder image and text when no room photo is available

### Stage 13
- Implemented Edit Window using AddEditWindowActivity in edit mode
- Implemented Edit Floor Space using AddEditFloorSpaceActivity in edit mode
- Added item option dialogs for windows and floor spaces with Edit and Delete actions
- Reused existing Add/Edit screens for updating records
- Updated Select Product screen to load real window and floor space names from Firestore instead of hardcoded labels

### Stage 14

- Integrated the provided Product API into SelectProductActivity
- Replaced hardcoded products with API-loaded window and floor products
- Ensured only window products are shown for windows
- Ensured only floor products are shown for floor spaces
- Improved product list display and product images
- Fixed product tab behaviour so empty rooms do not show fake window/floor tabs

### Stage 15

- Implemented window product compatibility constraints
- Prevented incompatible window products from being selected
- Added clear reason messages for invalid product selections
- Applied constraint logic for minimum height, maximum height, minimum width, maximum width, panel splitting, and panel count
- Improved recommended product behaviour for compatible window products

### Stage 16

- Added house search functionality on the main house list
- Enabled searching by customer name, address, and city
- Improved list usability and navigation for larger sets of house records

### Stage 17

- Improved room photo functionality
- Added support for both Gallery and Camera image selection
- Implemented a custom photo source dialog for a more professional UI
- Kept room images persisted in Firestore and visible after reopening the app
- Added full room image preview support

### Stage 18

- Improved quote generation and display
- Displayed itemised quote information per room
- Added room include/exclude checkboxes on the quote screen
- Recalculated totals immediately when rooms are included or excluded
- Improved itemised quote formatting for windows, floor spaces, labour cost, room total, and final total

### Stage 19

- Improved sharing functionality
- Changed quote sharing from plain text preview to a real CSV file attachment
- Added FileProvider support for Android sharing
- Confirmed CSV sharing works through the OS built-in share flow
- Improved quote export quality for HD+ submission expectations


## Test Device / Emulator
- Device: Medium Phone API 35 Emulator
- Orientation: Portrait

## Activities

### MainActivity
Displays the list of houses and allows the user to search houses, open a selected house, or create a new house.

### AddEditHouseActivity
Used to create a new house or edit an existing house. After saving, the user returns to the house list or house details flow.

### HouseDetailsActivity
Displays the selected house details and associated room list. From here the user can edit the house, add a room, open room details, generate a quote, or delete the house.

### AddEditRoomActivity
Used to create or edit a room within a house. Also supports adding a room photo from gallery or camera.

### RoomDetailsActivity
Displays the selected room and its associated windows and floor spaces. From here the user can edit the room, add windows, add floor spaces, preview the room image, or delete the room.

### AddEditWindowActivity
Used to create or edit a window record for a selected room.

### AddEditFloorSpaceActivity
Used to create or edit a floor space record for a selected room.

### SelectProductActivity
Used by both AddEditWindowActivity and AddEditFloorSpaceActivity to browse and select products from the provided Product API. It also applies window compatibility constraints when selecting window products.

### QuoteActivity
Generates and displays an itemised quote for the selected house, supports room include/exclude updates, recalculates totals, and shares the quote as a CSV attachment.

### RoomImagePreviewActivity
Displays a larger preview of the room image selected for a room.

## Custom Feature
- Apply same selection to all windows or floor spaces in a room

## Additional Usability Enhancement
Tapping a room image opens a larger preview screen so the photo can be viewed more clearly.

## References
- Firebase Firestore documentation
- Firebase Storage / Android image handling documentation
- Android Developers documentation for:
        - RecyclerView
        - Activity Result API
        - AlertDialog / Dialog
        - FileProvider
        - Sharing files with intents
- KIT721 lecture, tutorial, and assignment materials
- Product API provided in the assignment specification and rubric.
## GenAI Use
Generative AI (ChatGPT) was used during development for:
- planning implementation stages
- debugging Kotlin and XML issues
- improving UI wording and layout ideas
- generating example code snippets and implementation guidance for selected features
- structuring Firestore loading and nested collection handling
- implementing product API integration and window constraint logic
- improving quote generation and CSV sharing
- explaining runtime and compilation errors during development

ChatGPT support was used as guidance only. Any generated code or suggestions were reviewed, tested, adapted, and integrated by the student before inclusion in the final application. The final submitted application was assembled, modified, debugged, and validated by the student.

### Shared ChatGPT Link
- https://chatgpt.com/share/69edaa11-3664-839d-a2de-c70d03b94c11 — representative chat used for Android app development guidance on Firestore loading, add/edit/delete flows, product API integration, window constraints, camera/gallery image handling, quote generation, UI improvements, and CSV sharing.

### Prompts / Topics Used
Examples of prompts and topics discussed with ChatGPT included:
- how to load nested room, window, and floor space data from Firestore in Kotlin
- how to implement add/edit/delete flows for houses, rooms, windows, and floor spaces
- how to validate window product constraints such as min/max width, min/max height, panel splitting, and panel count
- how to implement gallery and camera selection for room photos
- how to generate and share a CSV quote file using Android FileProvider
- how to improve RecyclerView layouts, dialogs, and quote screen usability

The shared link above is representative of the type of guidance used during the assignment. Additional iterative support was also used across development.