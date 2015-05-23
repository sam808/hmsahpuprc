using System;
using System.Web;
using System.Web.Configuration;
using System.Collections.Generic;
using System.Text.RegularExpressions;
using Hmsa.SharedServices.Classes.SimpleData;

namespace Hmsa.SharedServices.Classes.URLRewrite
{
    public class URLRewriteHttpModule:IHttpModule
    {
        public URLRewriteHttpModule()
        {
 
        }

        public String ModuleName
        {
            get { return "URLRewriteHttpModule"; }
        }

        // In the Init function, register for HttpApplication 
        // events by adding your handlers.
        public void Init(HttpApplication application)
        {   
            application.BeginRequest += 
                (new EventHandler(this.Application_BeginRequest));
        }

        private void Application_BeginRequest(Object source, EventArgs e)
        {
            HttpApplication application = (HttpApplication)source;

            string rawURL = application.Request.Url.ToString().ToLower();

            string urlParityRegExPattern = GetSectionInConfigFile("URLParityRegExPattern");

            Match match = Regex.Match(rawURL, urlParityRegExPattern, RegexOptions.IgnoreCase);

            string rewritePath = string.Empty;

            if (match.Success)
            {
                //get id from filename in DB
                var filenames = rawURL.Split('/');
                string filename = filenames[filenames.Length - 1];

                if (filename.EndsWith(".htm"))
                {
                    Dictionary<string, string> indexFiles = GetIndexfiles();

                    if (indexFiles.ContainsKey(filename)) //Is Index file
                    {
                        rewritePath = String.Format("/{0}", indexFiles[filename]);
                    }
                    else //Is Policy
                    {
                        string policyPath = GetSectionInConfigFile("PolicyPath");
                        bool isInternalPolicy = rawURL.ToLower().Contains(policyPath.ToLower());

                        string policyRegExPattern = isInternalPolicy ? GetSectionInConfigFile("InternalPolicyRegExPattern") : GetSectionInConfigFile("ExternalPolicyRegExPattern");

                        Match m = Regex.Match(filename, policyRegExPattern, RegexOptions.IgnoreCase);

                        if (m.Success)
                        {
                            string policyName = string.Format("{0}", m);

                            int policyId = -1;

                            if (isInternalPolicy) //Internal Policy
                            {
                                policyId = HMSASimpleData.GetInternalPolicyIdByPolicyName(policyName);
                            }
                            else //External Policy
                            {
                                policyId = HMSASimpleData.GetExternalPolicyIdByPolicyName(policyName);
                            }

                            rewritePath = string.Format("{0}{1}", policyPath, policyId);
                        }
                    }
                }
                else //Is PDF, Excel, Doc, etc
                {
                    rewritePath = string.Format("{0}{1}", GetSectionInConfigFile("OtherDocsPolicyPath"), filename);
                }

                //Redirect
                HttpContext myContext = HttpContext.Current;

                myContext.Response.Redirect(rewritePath);
            }
        }

        private Dictionary<string, string> GetIndexfiles()
        {
            Dictionary<string, string> indexFiles = new Dictionary<string, string>();

            indexFiles.Add("zar_index.htm", "zar_index");
            indexFiles.Add("zav_in.med-index.htm", "MED-INDEX");
            indexFiles.Add("zav_in.ch-index.htm", "CH-INDEX");
            indexFiles.Add("zav_in.fh-index.htm", "FH-INDEX");
            indexFiles.Add("zav_in.rt-index.htm", "RT-INDEX");
            indexFiles.Add("zav_in.vs-index.htm", "VS-INDEX");
            indexFiles.Add("zav_in.qu-index.htm", "QU-index");
            indexFiles.Add("zav_in.dm-index.htm", "DM-INDEX");
            indexFiles.Add("zav_in.mp-med_pharm.htm", "MP-Med_Pharm");
            indexFiles.Add("zav_in.rx-pharmacies_-_drug_plans.htm", "RX-Pharmacies_-_Drug_Plans");

            return indexFiles;
        }

        private string GetSectionInConfigFile(string key)
        {
            string value = string.Empty;

            value = WebConfigurationManager.AppSettings[key];
      
            return value;
        }

        public void Dispose() { }
    }
}
