import {fetchWithAuth} from './auth.js';

export async function getUserData() {
    try {
        const response = await fetchWithAuth('/api/v1/me', {
            method: 'GET'
        });

        if (response.ok) {
            const data = await response.text()
            console.log('Username:', data);
        } else {
            console.log('Error fetching user data');
        }
    } catch (error) {
        console.error('Failed to fetch user data:', error);
    }
}