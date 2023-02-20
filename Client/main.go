package main

import (
	"bufio"
	"context"
	"fmt"
	"net"
	"os"
	"os/signal"
	"strings"
	"syscall"
)

func main() {
	signalChan := make(chan os.Signal, 1)
	fmt.Println("Preparing to receive input")
	reader := bufio.NewReader(os.Stdin)
	ctx, cancel := context.WithCancel(context.Background())
	connection, err := net.Dial("tcp", fmt.Sprintf("%v:%v", "localhost", "8080"))
	if err != nil {
		fmt.Printf("Unable to connect to server, with error: %v\n", err.Error())
		cancel()
		panic(1)
	}
	messageChannel := make(chan string)
	go func() {
		for {
			select {
			case <-ctx.Done():
				return
			case newMessage := <-messageChannel:
				go func() {
					_, err := fmt.Fprintln(connection, newMessage)
					if err != nil {
						fmt.Println("Unable to send the message")
						return
					}
				}()
			}
		}
	}()
	fmt.Println("Start now")
	go func() {
		for {
			select {
			case <-ctx.Done():
				return
			default:
				fmt.Print("-> ")
				text, _ := reader.ReadString('\n')
				processed := strings.Replace(text, "\n", "", -1)
				messageChannel <- processed
			}
		}
	}()
	signal.Notify(signalChan, os.Interrupt, syscall.SIGTERM)
	<-signalChan
	fmt.Println("Stopping now...")
	defer func() {
		cancel()
		connection.Close()
		close(messageChannel)
	}()
	close(signalChan)
	fmt.Println("Completed shutdown!")
}
