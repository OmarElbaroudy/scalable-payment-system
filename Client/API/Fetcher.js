export class fetcher {
    static async register(userName, password) {
        try {
            const requestOptions = {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({userName: userName, password: password})
            };
            const res = await fetch('http://localhost:3000/register', requestOptions)
            return res.json();
        } catch (e) {
            console.log(e);
        }
    }

    static async login(userName, password) {
        try{
            const requestOptions = {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({userName: userName, password: password})
            };
            const res = await fetch('http://localhost:3000/login', requestOptions)
            return res.json();
        }catch (e){
            console.log(e);
        }
    }
}
