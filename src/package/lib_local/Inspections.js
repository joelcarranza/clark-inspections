var Logger = require('Logger');
var WM = require('WM');

exports.inspectionCompleted = function(db, id) {
	Logger.info("inspectionCompleted = "+id);
	var listID = db.queryValue('SELECT LIST_ID FROM WM_INSPECTION WHERE ID = ?', id);
	if(listID) {
		let row = db.queryRow(`with list_stats as (
		select 
		    list_id, 
		    count(*) as total,
		    count(completion_status) as completed
		from wm_inspection
		group by list_id)
		SELECT 
            ls.completed as completed,
            ls.total as total,
            l.COMPL_FLAG,
            o.ORDER_KEY,
            l.CREATE_USER
        FROM WM_INSPECTION_LIST l 
        LEFT JOIN LIST_STATS ls on l.id = ls.list_id
		LEFT JOIN WM_ORDER o ON l.SERVICE_ORDNBR = o.ordnbr
	    WHERE l.id = ?`, listID);
		if(row) {
			let total = row[0];
			let completed = row[1];
			let complFlag = row[2];
			let orderKey = row[3];
			let listUser = row[4];

			if(total == completed && complFlag == 'N') {
				db.execute(`UPDATE WM_INSPECTION_LIST 
						SET COMPL_FLAG='Y', 
						    COMPL_DATE=GETDATE(),
							LOCKVERSION=LOCKVERSION+1 
						WHERE ID=?`, listID);

				if(orderKey) {
					db.execute(`UPDATE WM_ORDER 
						SET 
							COMPLTN_TS=GETDATE(), 
							COMPLTN_USER=?, 
							LOCKVERSION=LOCKVERSION+1 
						WHERE ORDER_KEY=?`, listUser, orderKey);
					WM.sendCompletionMessage(db, listUser, orderKey);
		          	WM.notifyViaSMTP(db, listUser, orderKey);
		        }
		        else {
		        	Logger.info('No service order found for WM_INSPECTION_LIST.ID', id);
		        }
			}


		}
		else {
			Logger.warn("No list found with id = " + listID);
		}
	}
	else {
		Logger.warn("no inspection found for id = " + id);
	}
	
}