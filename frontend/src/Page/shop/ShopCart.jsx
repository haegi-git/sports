function ShopCart(){
    return(
        <div>

            <h1>장바구니</h1>

            <table>
                <thead>
                <tr>
                    <th>First Name</th>
                    <th>Last Name</th>
                    <th>Phone</th>
                    <th>Email</th>
                    <th>Date of Birth</th>

                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>James</td>
                    <td>Matman</td>
                    <td>(713) 123-8965</td>
                    <td><a href="mailto:jmatman@stewart.com">jmatman@stewart.com</a></td>
                    <td>01/13/1979</td>
                </tr>
                <tr>
                    <td>Johnny</td>
                    <td>Smith</td>
                    <td>(713) 584-9614</td>
                    <td><a href="mailto:jsmith@stewart.com">jsmith@stewart.com</a></td>
                    <td>06/09/1971</td>
                </tr>
                <tr>
                    <td>Susan</td>
                    <td>Johnson</td>
                    <td>(713) 847-1124</td>
                    <td><a href="mailto:sjohnson@stewart.com">sjohnson@stewart.com</a></td>
                    <td>08/25/1965</td>
                </tr>
                <tr>
                    <td>Tracy</td>
                    <td>Richardson</td>
                    <td>(713) 245-4821</td>
                    <td><a href="mailto:trichard@stewart.com">trichard@stewart.com</a></td>
                    <td>03/13/1980</td>
                </tr>
                </tbody>
            </table>

        </div>
    )
}

export default ShopCart;