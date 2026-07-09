/*
 * NOTICES
 * -------
 * 
 * Copyright 2026 by Gatekeeper Systems All Rights Reserved.
 * 
 * Unpublished Work -- Protected under the copyright laws of the United States.
 * 
 * Restricted Rights Legend: Use, duplication or disclosure of the software
 * contained hereon is governed by the terms of a license agreement.  In
 * the absence of an agreement, use, duplication or disclosure by the United
 * States Government is subject to restrictions stated in subparagraph
 * (c)(1) of the Commercial Computer Software -- Restricted Rights clause
 * at FAR 52.227-9 or subparagraph (c)(1)(ii) of the Rights in Technical
 * Data and Computer Software clause at DFARS 252.227-7013, as applicable.
 * 
 * Contractor/Manufacturer:
 * 
 *     Gatekeeper Systems
 *     1010 E. Union St.
 *     Pasadena, CA 91106
 * 
 *     Tel: (626) 449-3070 or (800) 424-3070
 *     Fax: (626) 440-1742
 * 
 *     E-Mail: info@gatekeeper.com
 *     URL:    http://www.gatekeeper.com/
 *
 */

 
var JavaLink = require('JavaLink');
var Config = require('Config');
var Sql = require('Sql');
var Logger = require('Logger');
var Lang = require('Lang');

JavaLink.useDefaultDataSource();

JavaLink.updateFiles = function(files, id) {
  var existingFiles = this.db.queryColumn('SELECT id FROM MWM_ORDER_FILE WHERE INSPECTION_ID=?', id);
  var newFiles = [];
  
  files.forEach((f) => {
   let id = f.id;
    let ix = existingFiles.indexOf(id);
    if(ix == -1) {
      newFiles.push(id);
    }
    else {
      existingFiles.splice(ix, 1);
    }
  });  

  if(newFiles.length > 0) {
    let sth = this.db.prepare("UPDATE MWM_ORDER_FILE SET INSPECTION_ID = ? WHERE ID = ?");
    newFiles.forEach((id) => {
      sth.execute(id, id);
    });
    sth.close()
  }

  // these can be removed
  if(existingFiles.length > 0) {
    let sth = this.db.prepare("UPDATE MWM_ORDER_FILE SET INSPECTION_ID = NULL WHERE ID = ?");
    existingFiles.forEach((id) => {
      sth.execute(id);
    });
    sth.close()
  }
}

JavaLink.process = function() {
	 var data = this.thawData([
        'ID',
        {name:'FILES', fields: [
         'ID'
        ]},
        'COMPLETION_STATUS?',
        'ISSUE_PRIORITY?',
        'COMMENT?',
        'RESOLUTION_STATUS?',
        'RESOLUTION_WORK_ORDER?',
        'RESOLUTION_COMMENT?',        
      ]);
     var literals;
     if(data['COMPLETION_STATUS']) {
        literals = {
            COMPLETION_DATE: 'GETDATE()',
            COMPLETION_USER: Sql.quote(this.environment['REMOTE_USER']) 
        }
     }
     else {
        literals = {
            COMPLETION_DATE: 'NULL',
            COMPLETION_USER: 'NULL' 
        }
     }

    var files = Lang.take(data, 'FILES');     

    this.updateFiles(files, data.ID);
	 this.db.updateRow('WM_INSPECTION', data, 'ID', literals);
};

JavaLink.run();
