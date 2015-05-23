using System;
using Simple.Data;

namespace Hmsa.SharedServices.Classes.SimpleData
{
    public static class HMSASimpleData
    {
        public static int GetExternalPolicyIdByPolicyName(string policyName)
        {
            int result = -1;
            string likeSTM = string.Format("%/{0}", policyName);
            
            var db = Database.OpenNamedConnection("HMSALiveDB");
           
            var docs = db.Orchard_Autoroute_AutoroutePartRecord.All()
                        .Select(db.Orchard_Autoroute_AutoroutePartRecord.Id)
                        .Where(db.Orchard_Autoroute_AutoroutePartRecord.DisplayAlias.Like(likeSTM))
                        .FirstOrDefault();

            if (docs != null)
            {
                result = docs.Id;
            }
            
            return result;
        }

        public static int GetInternalPolicyIdByPolicyName(string policyName)
        {
            int result = -1;

            var db = Database.OpenNamedConnection("HMSALiveDB");

            var docs = db.ContentDocNumber.FindAllByDocNumber(policyName).FirstOrDefault();

            if (docs != null)
            {
                result = docs.ContentItemRecord_id;
            }        
        
            return result;
        }
    }
}
