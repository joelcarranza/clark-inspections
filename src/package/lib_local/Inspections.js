var Logger = require('Logger');

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
            l.COMPL_FLAG
        FROM WM_INSPECTION_LIST l 
        LEFT JOIN LIST_STATS ls on l.id = ls.list_id
	    WHERE l.id = ?`, listID);
		if(row) {
			let total = row[0];
			let completed = row[1];
			let complFlag = row[2];

			if(total == completed && complFlag == 'N') {
				db.execute(`UPDATE WM_INSPECTION_LIST SET COMPL_FLAG='Y', COMPL_DATE=GETDATE() WHERE ID=?`, listID);
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