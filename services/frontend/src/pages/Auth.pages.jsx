import React from 'react'
import { Navigate, Route, Routes } from 'react-router'
import GenericHeader from '../components/GenericHeader'
import LoginPage from './login.page'
import NotFoundPage from './NotFound.page'
import RegisterPage from './Register.page'

const authNavigation = [
	{
		content: 'login',
		to: '/auth/login',
		className: 'auth-btn',
	},
	{
		content: 'sign up',
		to: '/auth/sign-up',
		className: 'auth-btn',
	},
]

const AuthPages = () => {
    return (
        <div className='AuthPages'>
            <GenericHeader navigation={authNavigation} />

            <Routes>
                <Route path='' element={<Navigate to='login' />} />
                <Route path='login' element={<LoginPage />} />
                <Route path='sign-up' element={<RegisterPage />} />
                <Route path='*' element={<NotFoundPage />} />
            </Routes>
        </div>
    )
}

export default AuthPages