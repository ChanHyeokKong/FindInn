$(function() {
    const editor = new FroalaEditor('#editor', {
        height: 400
    });
    $('#hotelSelector').on('change', function() {
        const hotelId = $(this).val();

        if (!hotelId) {
            return;
        }

        $.ajax({
            url: '/api/hotel/description/' + hotelId,
            type: 'GET',
            success: function(response) {
                editor.html.set(response.content);
            },
            error: function(xhr, status, error) {
                console.error("Failed to fetch hotel description:", error);
                editor.html.set("<p>Error: Could not load content.</p>");
            }
        });
    });
});