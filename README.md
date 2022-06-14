### Project theme: Store Management Software

### Brief introduction: 
The program is based on a Client-Server structure that accesses a remote database. 2 roles have been created  (Customer and Shop Manager). Each role can access certain functions of the program.  
Each new user is registered as a "customer".  Please register with a "real" email  to receive any updates on the same, in case of order update. 

### Customer Features: 
-	Dashboard: Welcome card with useful data such as (money spent in the current month, orders and more), shortcuts to "Settings", possibility to send e-mail to staff
-	Products: A view of all available products, which can be added to a Shopping Cart
-	Shopping Cart: A display of all products in the user's cart. The products in the cart are reserved for 30 minutes. After the expiration of the "cart session", the quantities in the inventory are restored. Periodically (every minute), it is checked if the database still keeps the products confidential in such a way as to always show the user the updated data.
-	Checkout: Entering data such as address, telephone number and finalization of the purchase (the purchase does not require a real payment for this project).
-	Orders: The history of all orders placed, the status of the order and the last note sent to the customer 
-	Settings: Change password and change personal information.

### Shop Manager Features: 
-	Dashboard: Welcome card with useful data such as (online users, registered users, total orders, orders to be shipped, total prodducts and others)
-	Inventory of products: View, Insert, Edit, Delete products.
-	Product Categories: View, Insert, Edit, Deleteand Categories. 
-	Orders: View, Edit, Cancel Orders. Possibility to assign a "Status" to the order that will affect statistics and numerical data in dashboards. Also if the order status is changed, an e-mail will be sent to the customer.
-	Statistics: Statistical data for the chosen period. (Pie chart of most sold products and Bar chart related to earnings)
-	Users : Viewing, Inserting, Editing, Deleting Users. 
-	Settings: Change password and change personal information. In addition, it allows you to make some changes to the store such as: 
"Store Name". Or configure the mail server (email, host, password).

### Other Project Features:
-	Sending E-mails: When updating an order status, the customer will receive an e-mail. In addition, the customer can send an email to the admin.
-	Password Generation: Each user can request a new password directly from 
"Login → Forgot Password".  After that, an email will be sent  to the user, containing the new password.
-	Database Connection Pool: To improve query performance.
-	Secure saving of the password of the e-mail server: The program gives the user the possibility to configure his own email server. Then the Client sends an encrypted password to the server, which then decrypts it when it needs to send an email. AES was used with a symmetric key. 
-	Serializable class, bridge between Image and Blob: Special methods have been created to allow the transfer and conversion from Blob to Image and vice versa. 
-	Implemented Dark Mode, possibility to choose the mode of the color theme (LIGHT, DARK, AUTO). The latter detects the theme only when it is selected da "Settings → App Preferences". Your preference will be stored for the next time you sign in.
NB: if you want to open FXML files in SceneBuilder (8.5.0) and import the jar files  contained in the project in the folder "/src/main/resources/application/*. jar".

### Technology Used:
- Java (jdk 15.0.2)
- JavaFX and SceneBuilder (8.5.0)
- jfoenix, animateFX, controlsfx, fontawesomefx (external libraries for GUI components)
- CSS (for styling graphical components)
- PHP (a script has been written to make sure the items in customers' carts are reserved for 30 minutes. After the expiration of the "cart session", the quantities in the inventory are restored. Periodically (every minute), it is checked if the database still keeps the products confidential in such a way as to always show the user the updated data. This choice has been done, because the database was located remotely on a hosting server, which allowed only chron jobs to be executed, disabling the possibility to use routines in MySQL)
- Database Connection Pool (for better performance)
- Usage of External Mail Server (The data of port, server name, password can be set directly in the database through the "Settings" section in the "Store management Software". Password is securely read and saved with encryption and decryption methods using AES256)
- Sentry (Log System for monitoring erros and performance online)

### Snapshots:
