'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database.ref('/notifications/{user_id}/{notification_id}').onWrite(event => {

  const user_id = event.params.user_id;
  const notification_id = event.params.notification_id;
  console.log('We have notification to send to', user_id);
  if(!event.data.val()){
    return console.log('A new notification has been deleted from database ',notification_id);
  }
  const fromUser = admin.database().ref(`/notifications/${user_id}/${notification_id}`).once('value');
    return fromUser.then(fromUserResult => {

      const from_user_id = fromUserResult.val().from;
      console.log('You Have new notification from : ',from_user_id);

      const userQuery = admin.database().ref(`Users/${from_user_id}/name`).once('value');
      const deviceToken = admin.database().ref(`/Users/${user_id}/device_token`).once('value');

      return Promise.all([userQuery,deviceToken]).then(result => {
          const username = result[0].val();
          const token_id = result[1].val();
          const payload = {
            notification: {
              title: "Friend Request",
              body: `${username} has sent you Friend Request`,
              icon: "default",
              click_action: "com.sh.chitchat_TARGET_NOTIFICATION"
            },
            data : {
              from_user_id : from_user_id
            }
          }
          return admin.messaging().sendToDevice(token_id,payload).then(response =>{
            console.log('this was the notification feature');
          });
      });
  });
});
