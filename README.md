# Container-Store

This container store project is an updated version of the very first project I ever attempted by myself, regarding not copying or learning from somebody’s example. Written in JAVA and SQL
	
I started this project as I myself worked in a removal business and have first-hand experience in what this project was trying to solve, mainly moving the customers container and locations into a database, instead of just using a book for writing in, as well as having all customers details and inventories at a touch of a button, the most helpful being the searching and moving of containers and the quick process of creating a picklist for multiple customers containers.

As I said this was the first project I did this where I learned a lot from, so though I didn’t have to much difficulty this time around, the first time was a very slow and time consuming process where everything had to be looked up on how to do, especially the SQL for the database, this time around i added filechoosers, item listeners to jtables, as well as action listners for windows, there was only one new added feature, which I am most proud of as it was a feature I wanted for the first one but was way beyond my comprehension at the time, that feature is the move container part, whereas before I had it so you had to manually move every container by entering the container number the location every time, which can be a lot if you got a container out from the back row now all the ones in front have to be moved up one space.
But the new feature paints little containers in the order and position of the containers in the database by row, and you can now just click on container click remove and all the other containers will be shuffled up and the database updated, and also allows for containers to be added from the floating list (containers not in a row) back into the row by clicking and dragging, I am extremely pleased how this came out, and if I still worked in removals it would definitely be giant time saver.
Though I had not attempted this before I had taken inspiration in how to create it from programming the blocks in the game breakout, this is also where I learned about the mouse motion listeners.
I also added just for fun really container accolades for seeing how old a container is how many times it has been moved etc.

There is one main issue that I have learned from I have made it create too many databases and used an outside database SQLite, If I were to do it again I would cut the number of databases down and use a native database i.e., JavaDB.

Coding wise its not horrendous but going through it later with more experience it can definitely be improved and optimised much better, but i am happy my naming of things and comments in methods were good enough that coming back to it after a year or so i could easily navigate and spot mistakes and bug fixes.





USER MANUAL

Starting the program;
	On first opening the program you are presented with the administration log in screen this is 		where the first admin on the program will enter the username and password, on completion you will 	be asked to input the username and password in again.

Setting up the warehouse;
	You will be presented with a window prompting you to set the warehouse specifics, IMPORTANT you 		must complete this to go further, the window will not close until complete.
	Start with the containers in the input fields you type the starting number e.g.  1 and the last 		container number e.g., 300 and click the generate button this will automatically create all the 		container numbers from the first to last. Then move onto the location generator, type how many 		rows in your warehouse, this is based on an alphabetical row allocation entering higher number 		than 26 is not allowed, enter how many containers are stacked per column in the row and then how 		many columns per row and click generate.
	Await completion of loading bars as details are being added to the database, the program will 		open.

Add new Customer;
	Enter all fields for entering a new customer, address is best entered as such
	123
	The street
	That-way-this-way
	Po 234ju
		(Broken up with enter key)
	Entering the containers and their locations, type in the container number (only numbers accepted)
	Then type where the container is to be located, a drop-down box will help for easy find, then 		click add, repeat until all containers added, if a new container number is added that is not in 		the system you will be given the option to add it to the system, does not work with locations! 		Checks are made for duplicates, when all is complete click next.

after a new customer is added along with their containers and inventorys on the main screen you 	can click on the custromers name form the customer table and their conatiners and locations will 	be visible in the containers table to the right of it, there you can click on a container to view 	its contaents in the inventory table below


Adding inventory;
	The added containers will be listed in the drop-down box, click add new item and fill out the 			details, click on the last column labelled photo to add photo file, on completing the inventory 		hit save, when all containers completed hit finish, if a container didn’t have inventory added it 	will show up in the button on the home screen, create new inventory.

Edit customer details

Enter customers last name or ID number (created automatically in add new customer) and click 			find, can search by first letter of name, and it will bring up all customers whose last name 			begins 	with that letter, add more letters for more refined search click on name in drop down-box 	to auto fill details, amend accordingly. And save.

Search customer;
	Enter name of customer to search using drop-down list, details shown

Remove customer;
	Enter name of customer to be removed from system, click remove, barrier to customers with 		inventoried containers, message asking to empty container will appear, click remove to remove.

Add new container;
	To add new container to system or straight to customer, to system adds to the empty container 			table, to customer you must provide which customer and a location.

Move containers;
	Window opens up display a drop-down menu of all aisle entered in system, choose aisle to see all 		containers places entered, allocated containers will be found in there given location, click on 		container(s) and remove, they will be added to the floating table in the database, they will 			appear in a line underneath the aisles they can be added back by clicking and dragging, they will 	automatically go to the next space wherever they are placed, alternatively they can be dragged to 	the red box saying empty which will empty the container and unassign it from the customer.

Search container;
	Enter container number to search for its location.

Empty container;
	Enter container to be emptied, if customers last container there will be option to remove 	customer.

Create picklist;
	Enter customers name and search in drop-down box, choice to add all containers with ADD* button 		or individually select from drop-down box ADD or from there add a single item to be picked from 		container ADD ITEM, full details on customer and location added to list, hit FINALIZE to remove 		containers and or inventories/item from database, or just click print if items are to be searched 	but not removed.

Create new inventory;
	Inventory list off all allocated containers with no items in inventory.

Search inventory;
	Search by container or only item only, description extra, will search all containers if no 	container added. 

Edit inventory;
	For moving items between existing containers, enter container number then press enter, add item 	or remove item or 	toggle relocate to container to search other container to move items 	between.

View customer history;
	Search customer to find out action history

View container history;
	Search container to find out its history

View activity history;
	View user activity

View Analytics;
	View miscellaneous data about containers and users.



ADMIN CONTROLS

Add new user;
	Adds new basic user or admin

Remove user;
 	Remove users

Change passwords;
	Change user passwords

Remove container;
	Removes container from system (container beyond repair)

Manage warehouse;
	Add new aisles or containers to warehouse
