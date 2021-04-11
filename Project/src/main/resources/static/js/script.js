function clearSearch() {
    window.location = $("#path").val();
}

$(document).on("click", ".open-delete-dialog", function () {
    $("#deleteId").val($(this).data('id'));
});

$(document).on("click", ".open-block-dialog", function () {
    $("#blockId").val($(this).data('id'));
});


$(document).on("click", ".open-unblock-dialog", function () {
    $("#unblockId").val($(this).data('id'));
});


$(document).ready(function () {
    selectSortOption();
});

function selectSortOption() {
    $('#sortOptionSelect').change(function (evt) {
        window.location.replace($("#path").val() + '?pageSize=' + $("#selectedPageSize").val() + '&page=1&keyword=' + $("#keyword").val()
            + '&sortOption=' + this.value + '&sortDirection=' + $("#sortDirection").val());
    });
}


$(document).ready(function () {
    changePageSize();
});

function changePageSize() {
    $('#pageSizeSelect').change(function (evt) {
        window.location.replace($("#path").val() + '?pageSize=' + this.value + '&page=1&keyword=' + $("#keyword").val() + '&sortOption=' + $("#selectedSortOption").val() + '&sortDirection=' + $("#sortDirection").val());
    });
}

 $(document).ready(function() {
    $('body').on('click', '.update',   function() {
        let id = $( this ).parent().find('input[name="id"]').val();
        let quantity = $( this ).parent().find('input[name="quantity"]').val();
        let url = '/orders/editOrder/update?id=' + id + '&quantity=' + quantity;

        $('#replace_div').load(url);
    });
 });

$(document).ready(function() {
    $('body').on('click', '.update-draft',   function() {
        let id = $( this ).parent().find('input[name="id"]').val();
        let quantity = $( this ).parent().find('input[name="quantity"]').val();
        let url = '/orders/orderData/update?id=' + id + '&quantity=' + quantity;

        $('#replace_div').load(url);
    });
});


$(document).on("click", ".open-remove-dialog", function () {
    let passedID = $(this).data('id');
    $("#removeId").val(passedID);
});

function addOrderCancelItem() {
    window.location = '/orders/addItem/cancel';
}

function editOrderCancelItem() {
    window.location = '/orders/editOrder/addItem/cancel';
}


