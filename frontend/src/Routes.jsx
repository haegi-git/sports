import Home from "./Page/Home";
import Login from "./Page/user/Login";
import Layout from "./Page/Layout";
import { createBrowserRouter } from "react-router-dom";
import Register from "./Page/user/Register";
import React from "react";
import Sports from "./Page/Sports";
import SportsChild from "./Page/SportsChild";
import Shop from "./Page/shop/Shop";
import ShopAdd from "./Page/shop/ShopAdd";
import ShopDetail from "./Page/shop/ShopDetail";

const Routes = createBrowserRouter([
    {
        element: <Layout/>,
        children: [
            { path: '/', element: <Home/> },
            { path: '/login', element: <Login/> },
            { path: '/register', element: <Register/> },
            { path: '/sports', element: <Sports/>,
                children:[{
                    path: '/sports/:sport', element: <SportsChild/>
                }]
            },
            { path: '/shop', element: <Shop/> },
            { path: '/shop/detail/:id', element:<ShopDetail/>},
            { path: '/shop/add', element: <ShopAdd/>}
        ]
    }
]);

export default Routes;
