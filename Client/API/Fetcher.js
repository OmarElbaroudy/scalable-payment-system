export class fetcher {
    static async register(userName, password) {
        try {
            const requestOptions = {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({userName: userName, password: password})
            };
            const res = await fetch('http://localhost:3000/register', requestOptions);
            return res.json();
        } catch (e) {
            console.log(e);
        }
    }

    static async login(userName, password) {
        try {
            const requestOptions = {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({userName: userName, password: password})
            };
            const res = await fetch('http://localhost:3000/login', requestOptions);
            return res.json();
        } catch (e) {
            console.log(e);
        }
    }

    static async balance(userId) {
        try {
            const myHeaders = new Headers();
            myHeaders.append('Content-Type', 'application/json');
            myHeaders.append('userId', userId);
            const requestOptions = {
                method: 'GET',
                headers: myHeaders
            };
            const res = await fetch('http://localhost:3000/getBalance', requestOptions);
            return res.json();
        } catch (e) {
            console.log(e);
        }
    }

    static async transfer(senderId, Receiver, amount) {
        try {
            const myHeaders = new Headers();
            myHeaders.append('Content-Type', 'application/json');
            myHeaders.append('userId', senderId);
            const requestOptions = {
                method: 'POST',
                headers: myHeaders,
                body: JSON.stringify({userName: Receiver, amount: amount})
            };
            const res = await fetch('http://localhost:3000/transfer', requestOptions);
            return res.json();
        } catch (e) {
            console.log(e);
        }
    }

    static async buy(userId, amount) {
        try {
            const myHeaders = new Headers();
            myHeaders.append('Content-Type', 'application/json');
            myHeaders.append('userId', userId);
            const requestOptions = {
                method: 'POST',
                headers: myHeaders,
                body: JSON.stringify({amount: amount})
            };
            const res = await fetch('http://localhost:3000/buy', requestOptions);
            return res.json();
        } catch (e) {
            console.log(e);
        }
    }

    static async sell(userId, amount) {
        try {
            const myHeaders = new Headers();
            myHeaders.append('Content-Type', 'application/json');
            myHeaders.append('userId', userId);
            const requestOptions = {
                method: 'POST',
                headers: myHeaders,
                body: JSON.stringify({amount: amount})
            };
            const res = await fetch('http://localhost:3000/sell', requestOptions);
            return res.json();
        } catch (e) {
            console.log(e);
        }
    }
}
