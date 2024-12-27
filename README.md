#### Student ID: s3924577
#### Student Name: Nguyen Nguyen Khuong

# Functionality of the App
The Vein - Blood Donation Site Map provides the following features:

+ Map View:

	- Displays nearby blood donation sites based on the user's location.
	- Customized markers to represent donation sites.
	- Clickable markers show site details, including address, donation hours, and required blood types.

+ Filter/Search:

	- Users can filter donation sites by criteria such as distance, blood type needed and event date.

+ User Registration and Login:

	- Secure authentication system to register and log in users.

+ Donor Features:

	- View donation sites and register for events.
	- Receive notifications about updates or changes to donation sites.
	- Navigate from the current location to a donation site.

+ Site Manager Features:

	- Create and manage blood donation sites.
	- View registered donors and volunteers for their sites.
	- Input post-event data such as blood volume and types collected.
	- Register themselves as volunteers for other donation sites.

+ Super User Features:

	- Access all events and their details.
	- Generate reports on donation drives, including donor counts and blood collection metrics.

# Technology Used

+ Android Studio:

	- Primary IDE for developing the Android application.
	- Used to design the app's UI and manage backend functionality.

+ Google Maps API:

	- Enables map-based visualization of donation sites.
	- Provides routing features to navigate users to the nearest site.

+ Firestore Database: 

	- Stores local data such as user information and site details.

+ Firebase Cloud Messaging:

	- Delivers updates and alerts to users about site changes or new events.

# Drawbacks
+ Offline Functionality:

	- The app requires an internet connection for map and notification features, limiting usability in areas with poor connectivity.

+ Limited Real-Time Data Updates:

	- Updates to donation site details may not be instantly reflected across all user devices.

+ Scalability Challenges:

	- Performance may degrade with a high number of donation sites or users without server-side optimizations.
