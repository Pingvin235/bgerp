<%@ tag body-content="empty" pageEncoding="UTF-8" description="IP address"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="paramName" description="input element name"%>
<%@ attribute name="value" description="current value"%>
<%@ attribute name="selector" description="jQuery element selector"%>
<%@ attribute name="editable" description="=1, editable"%>

<c:set var="type" value="ip"/>

<c:if test="${not empty paramName}">
	<c:set var="uiid" value="${u:uiid()}"/>
	<c:set var="selector" value="#${uiid}"/>
	<input type="text" name="${paramName}" id="${uiid}"/>
</c:if>

<script style="display: none;">
<c:if test="${type eq 'ip'}">
	$("${selector}").inputmask("999.999.999.999");
</c:if>

var setFocusAndRangeForIp = function( from, end, event,  preventDefault )
{

	S='${selector}';

   if (preventDefault){
	   event.preventDefault();
   }
             setTimeout( function()
         {
             $(S)[0].setSelectionRange(from,end);
         }, 100 );
}

<%-- Focus --%>
$("${selector}").focus( function( event )
        {

                var start = this.selectionStart;
                this.setSelectionRange(0,0);
                    event.preventDefault();
                     var element = this;
                    setTimeout( function()
                    {
                            element.setSelectionRange(0,3);
                    }, 100 );
                return false;
  });

     $("${selector}").click(function(e) {

<%-- Mouse click handler--%>
         var start = this.selectionStart;
                              if (start<=3){
                            	  setFocusAndRangeForIp(0, 3 , e, false);
                              }else{
                                      if (start<=8){
                                    	  setFocusAndRangeForIp(4, 7, e, false);
                                      }else{
                                              if (start<=11){
                                            	  setFocusAndRangeForIp(8, 11, e, false);
                                              }else{
                                                      if (start<=14){
                                                    	  setFocusAndRangeForIp(12, 15, e, false);
                                                      }
                                              }
                                      }
                              }
     }
       );


     $("${selector}").keydown(function(e) {
             var start = this.selectionStart;
<%-- TAB key handler --%>
    if(e.keyCode === 9){

            if (start<=3){
                 setFocusAndRangeForIp(4, 7, e, true);
            }else{
                    if (start<=7){
                     setFocusAndRangeForIp(8, 11, e, true);
                    }else{
                            if (start<=11){
                             setFocusAndRangeForIp(12, 15, e, true);
                            }else{
                                    if (start<=15){
                                    		setFocusAndRangeForIp(0, 3, e, false);
                                    }
                            }
                    }
            }

    }

<%-- UP arrow button handler --%>
    if(e.keyCode === 38){
           if (start<=3){
        	   		setFocusAndRangeForIp(12, 15, e, true);
            }else{
                    if (start<=7){
                    	setFocusAndRangeForIp(0, 3, e, true);
                    }else{
                            if (start<=11){
                            	setFocusAndRangeForIp(4, 7, e, true);
                            }else{
                                    if (start<=15)
                                    	setFocusAndRangeForIp(8, 11, e, true);
                            }
                    }
            }

    }


<%-- Down arrow button handler --%>
    if(e.keyCode === 40){
            e.preventDefault();
            if (start<=3){
            	setFocusAndRangeForIp(4, 7, e, true);
            }else{
                    if (start<=7){
                    	setFocusAndRangeForIp(8, 11, e, true);
                    }else{
                            if (start==8){
                            	setFocusAndRangeForIp(12, 15, e, true);
                            }else{
                                    if (start<=13){
                                    	setFocusAndRangeForIp(0, 3, e, true);
                                    }
                            }
                    }
            }
    }
<%-- RIGHT arrow button handler --%>
       if(e.keyCode===39){
               if (start==3){
                setFocusAndRangeForIp(4, 7, e, true);
               }
               if (start==7){
                    setFocusAndRangeForIp(8, 11, e, true);
               }
               if (start==11){
                     setFocusAndRangeForIp(12, 15, e, true);
               }
               if (start==15){
                      setFocusAndRangeForIp(0, 3, e, true);
               }
       }

<%-- LEFT arrow button handler --%>

       if (e.keyCode===37){

               if (start==0){

                     setFocusAndRangeForIp(12, 15, e, true);
               }
               if (start==4){
                    setFocusAndRangeForIp(0, 3, e, true);
               }
               if (start==8){
                      setFocusAndRangeForIp(4, 7, e, true);
               }

               if (start==12){
                     setFocusAndRangeForIp(8, 11, e, true);

               }


       }
<%-- Keypad/NumPad buttons handler --%>
       if((e.keyCode === 48)||(e.keyCode===49)||(e.keyCode===50)||(e.keyCode===51)||(e.keyCode===52)||(e.keyCode===53)||(e.keyCode===54)||(e.keyCode===55)||(e.keyCode===56)||(e.keyCode===57)
                   ||(e.keyCode===96)||(e.keyCode===97)||(e.keyCode===98)||(e.keyCode===99)||(e.keyCode===100)||(e.keyCode===101)||(e.keyCode===102)||(e.keyCode===103)||(e.keyCode===104)||(e.keyCode===105)){
    	      if (start==2){
    	    	  setFocusAndRangeForIp(4, 7, e, false);
    	       }
    	       if (start==6){
	   		   	   setFocusAndRangeForIp(8, 11, e, false);
    	       }
    	       if (start==10){
    	    	   setFocusAndRangeForIp(12, 15, e, false);
    	       }
       }
    }
     );

    $("${selector}").css( "text-align", "center" );
</script>

<c:set var="type" value=""/>
<c:set var="editable" value=""/>