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
        // window.location.replace('/' + $("#entity").data('data-entity') + '/all/pageable?pageSize=' + $("#selectedPageSize").data('selectedPageSize') + '&page=1&keyword=' + $("#keyword").data('keyword')
        //     + '&sortOption=' + this.value + '&sortDirection=' + $("#sortDirection").data('sortDirection'));
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

// $(document).ready(function () {
//     editOrderUpdateOrderLine();
// });
//
// function editOrderUpdateOrderLine() {
//
//     let $buttons = Array.from(document.getElementsByClassName('update'));
//
//     $buttons.forEach(b => b.addEventListener('click', handler))
//
//     function handler(e) {
//
//         let $button = e.target;
//         let inputId = $button.parentElement.children[0];
//         let id = inputId.value;
//         let inputQuantity = $button.parentElement.children[1];
//         let quantity = inputQuantity.value;
//
//         let url = '/orders/editOrder/update';
//
//         $.get({
//             url: url,
//             data: {
//                 id: id,
//                 quantity: quantity
//             }
//         }).then(function () {
//             $('#replace_div').load(url);
//         });
//
//
//     }
// }



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

//set on change listener
//     $( '.update' ).click(function() {
//         let id = $( this ).parent().get(0);
//         let quantity = $( this ).parent().get(1);
//         let url = '/orders/editOrder/update?=' + id + '&quantity=' + quantity;
//
//         $('#replace_div').load(url);


    // function getContent() {
    //
    //     //create url to request fragment
    //     var url = /orders/;
    //     if ($('#selection').val() === "Content 1") {
    //         url = url + "content1";
    //     } else {
    //         url = url + "content2";
    //     }
    //
    //     //load fragment and replace content
    //     $('#replace_div').load(url);
    // }
// })

// $(document).ready(function () {
//     draftOrderUpdateOrderLine();
// });
//
// function draftOrderUpdateOrderLine() {
//
//     let $buttons = Array.from(document.getElementsByClassName('update-draft'));
//
//     $buttons.forEach(b => b.addEventListener('click', handler))
//
//     function handler(e) {
//
//         let $button = e.target;
//         let inputId = $button.parentElement.children[0];
//         let id = inputId.value;
//         let inputQuantity = $button.parentElement.children[1];
//         let quantity = inputQuantity.value;
//
//         // let url = '/orders/orderData/update';
//         let url = '/orders/orderData/update?id=' + id + '&quantity=' + quantity;
//
//         //
//         // $.get({
//         //     url: url,
//         //     data: {
//         //         id: id,
//         //         quantity: quantity
//         //     }
//         // }).then(function () {
//         $('#replace_div').load(url);
//         // });
//
//     }
// }

// $(document).ready(function () {
//     editOrderRemoveOrderLine();
// });

// function editOrderRemoveOrderLine(){
//
//     let $buttons = Array.from(document.getElementsByClassName('remove'));
//
//     $buttons.forEach(b => b.addEventListener('click', handler))
//
//     function handler(e){
//         let $button = e.target;
//         let $inputId = $button.previousElementSibling;
//         let id = $inputId.value;
//
//         let url = '/orders/editOrder/remove';
//
//         $.get({
//             url: url,
//             data: {
//                 id: id,
//             }
//         }).then(function () {
//             $('#replace_div').load(url);
//         });
//
//     }
// }

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


