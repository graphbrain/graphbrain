(ns graphbrain.web.cssandjs)

(defonce version "130113")

(defn- random-version
  []
  (str (rand-int 999999999)))

(defonce analytics-js
  "<script type='text/javascript'>
     var _gaq = _gaq || [];
     _gaq.push(['_setAccount', 'UA-30917836-1']);
     _gaq.push(['_trackPageview']);
     (function() {
         var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
         ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
         var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
    })();
  </script>")

(defn css+js
  []
  (str "<link href='/css/gb.css?" (random-version) "' type='text/css' rel='Stylesheet' />"
       "<script src='/js/jquery-1.7.2.min.js' type='text/javascript'></script>"
       "<script src='/js/jquery-ui-1.8.18.custom.min.js' type='text/javascript' ></script>"
       "<script src='/js/bootstrap.min.js' type='text/javascript' ></script>"
       "<script src='/js/gbui.js?" (random-version) "' type='text/javascript' ></script>"))
