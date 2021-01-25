# Project Bubble
Website: https://projectbubble.org/
### Overview
Geocoded 2.3 million voter data records in Connecticut and built a Restful API and website for anyone to see the voter record information for the 1000 geographically closest voters.  This project's purpose was intended for Connecticut politicians to use phone banking more efficiently by providing volunteers with people in their city or community instead of randomly assigned throughout the state.
### Data and Safety Information
All voter data was publicly provided by the [State of Connecticut](https://portal.ct.gov/SOTS/Election-Services/Statistics-and-Data/Statistics-and-Data), and all geocoding information was obtained using the [US Census Geocoding Services](https://geocoding.geo.census.gov/geocoder/Geocoding_Services_API.pdf) or [Geocodio](http://geocod.io/).  The structure of the database is public, but all of the data is kept private to prevent abuse.  All queries and website visits are logged and monitored for abuse protection.  To report abuse or to request data removal go to https://projects.suppiger.org/contact.php .
### Project Structure
This project was broken up into four main parts:
1. **Parsing Voter Data** - The voter data was parsed with Java and stored in a mysql database for manipulation.  Roughly 2,152 out of 2,420,302 voter records were improperly formatted or blank and thus lost in the extraction.
2. **Geocoding Addresses** - Using the Census Geocoding Service, 2,287,246 of the remaining 2,418,150 records were matched with 2,151,650 being exact matches.  The non-matched records were then analyzed by Geocodio which was able to give coordinates for all of the records at varying levels of accuracy.
3. **Back-end Development** - A back-end was built with Flask and MySQL with two main API endpoints:
	 1. Closest records: `/geo/<string:state>/closest/<int:record_count>`
	 2. Voter Information Lookup: `/voter/<string:state>/<string:voter_id>/info`
4. **Front-end Development** - Finally, the front-end was developed using mostly JQuery and Ajax with Bootstrap and WebGL for the UI.
### Project Expansion and Improvement
The reason Connecticut was chosen was since it was a smaller state with publicly available data, but geocoding every state is definitely possible with time and resources.  Voter data laws by state can be found [here](https://www.ncsl.org/research/elections-and-campaigns/access-to-and-use-of-voter-registration-lists.aspx).

A few huge improvements that would be made with expansion are:
- **Changing the database to MongoDB** - While the back-end was being developed, I realized a lot of this data will vary by state, and having a rigid structure like a relational database will be damaging.  Also, it should be noted the current structure in MySQL uses multicolumn indices to query the closest records effciently, but an R-Tree data structure would improve runtime.
- **Java Data Extraction Structure** - The structure for the Java application was not ideal, and needs to be more fluid and hierarchical.  The data should be cleaned up to some extent before the Java Application extracts and geocodes all of it.  The entire setup needs to be restructured using a State superclass or change the structure to be based around each step of extraction/geocoding.
- **Front-end** - The front-end should be moved to either Angular or React, and possibly the back-end could take more of the computataional calcultions that the front-end is currently handling.
