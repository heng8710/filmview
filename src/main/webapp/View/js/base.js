function ishare(api, pubid)
{
	pubid = pubid || '';

	switch(api)
	{
		case 'qr':
			$('#QRShareModal').modal('show');
			$('#QRShareImage').attr("src","http://api.qrserver.com/v1/create-qr-code/?size=200x200&data="+encodeURIComponent(window.location.href));
		break;

		case 'favorites':
			addToFavorite(window.location.href, document.title);
		break;

		default:
			window.open('http://api.addthis.com/oexchange/0.8/forward/'+ api +'/offer?pubid='+pubid+'&url='+encodeURIComponent(window.location.href)+'&title='+encodeURIComponent(document.title));
	}
}

function addToFavorite(url, title)
{
	if (window.sidebar)
	{
		// Mozilla Firefox Bookmark
		window.sidebar.addPanel(title, url,"");
	}
	else if(document.all && window.external)
	{
		// IE Favorite
		if(window.external.addToFavoritesBar)
		{
			window.external.addToFavoritesBar(url, title); //IE8
		}
		else
		{
			window.external.AddFavorite(url, title);
		}
	}
	else if(window.opera)
	{
		// Opera 7+
		document.getElementById("addlink").href = url;
		document.getElementById("addlink").title = title;
		document.getElementById("addlink").rel="sidebar";
	}
	else
	{
		//Chrome/Safari
		alert('Press ' + (navigator.userAgent.toLowerCase().indexOf('mac') != - 1 ? 'Command/Cmd' : 'CTRL') + ' + D to bookmark this page.');
	}
}

(function($){

//search-input//////////////////
$(document).ready(function(){

	//fullsearch-form
	$('.fullsearch-form').submit(function(){
		window.location.href = $(this).attr('action') +"/"+encodeURIComponent($('input[name=keyword]', this).val().replace(/\./g,'-'));
		return false;
	});
	
	if($('body').height() > $(window).height())
    {
        $("#prev, #next, #download, #play").hide();
    }

    var show = function(){
        return $('body').height() - ($(window).height() + $(window).height() / 2);
    }

    $(function () {
        $(window).scroll(function () {
            if ($(this).scrollTop() > show()) {
                $("#prev, #next, #download, #play").show();
            } else {
                $("#prev, #next, #download, #play").hide();
            }
        });
    });
});

})(jQuery);